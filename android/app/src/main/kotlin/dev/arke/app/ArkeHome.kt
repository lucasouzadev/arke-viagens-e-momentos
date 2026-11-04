package dev.arke.app

import androidx.annotation.StringRes
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.customActions
import kotlin.math.abs
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import kotlinx.coroutines.launch
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import dev.arke.core.PlaceIdea
import dev.arke.core.HomeState
import dev.arke.core.PanelDetent
import dev.arke.core.TravelContext

@Composable
private fun SharePinDialog(name: String, pin: LocalMapPin, onClose: () -> Unit) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var token by remember { mutableStateOf<String?>(null) }
    var trips by remember { mutableStateOf<List<Pair<String, String>>>(emptyList()) }
    var destination by remember { mutableStateOf<String?>(null) }
    var title by remember { mutableStateOf("") }
    var busy by remember { mutableStateOf(false) }
    var published by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    fun perform(action: suspend () -> Unit) {
        if (busy) return
        busy = true
        message = null
        scope.launch {
            try { action() }
            catch (failure: Exception) {
                if (failure is kotlinx.coroutines.CancellationException) throw failure
                message = "Não foi possível concluir. Confira a conexão e suas permissões."
            } finally { busy = false }
        }
    }

    val session = token
    AlertDialog(
        onDismissRequest = { if (!busy) onClose() },
        title = { Text("Compartilhar Pin") },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                Text("Compartilhará o nome e as coordenadas de $name com os membros da viagem escolhida.")
                if (session == null) {
                    OutlinedTextField(email, { email = it }, label = { Text("E-mail") }, singleLine = true, enabled = !busy)
                    OutlinedTextField(password, { password = it }, label = { Text("Senha") }, singleLine = true, enabled = !busy,
                        visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation())
                } else if (!published) {
                    trips.forEach { (id, tripTitle) ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(selected = destination == id, onClick = { destination = id }, enabled = !busy)
                            Text(tripTitle)
                        }
                    }
                    OutlinedTextField(title, { title = it }, label = { Text("Nome da nova viagem compartilhada") }, singleLine = true, enabled = !busy)
                    TextButton(enabled = title.isNotBlank() && !busy, onClick = {
                        perform {
                            val created = org.json.JSONArray(arkeCollaborationRequest("/rest/v1/shared_trips", session,
                                org.json.JSONObject().put("title", title), representation = true)).getJSONObject(0)
                            val id = created.getString("id")
                            trips = trips + (id to created.getString("title"))
                            destination = id
                            title = ""
                        }
                    }) { Text("Criar viagem compartilhada") }
                }
                message?.let { Text(it) }
            }
        },
        confirmButton = {
            if (!published) TextButton(
                enabled = !busy && (if (session == null) email.isNotBlank() && password.isNotEmpty() else destination != null),
                onClick = {
                    perform {
                        if (session == null) {
                            val response = try {
                                arkeCollaborationRequest("/auth/v1/token?grant_type=password", body =
                                    org.json.JSONObject().put("email", email).put("password", password))
                            } finally { password = "" }
                            val access = org.json.JSONObject(response).getString("access_token")
                            val rows = org.json.JSONArray(arkeCollaborationRequest("/rest/v1/shared_trips?select=id,title&order=title", access))
                            trips = (0 until rows.length()).map { index ->
                                rows.getJSONObject(index).let { it.getString("id") to it.getString("title") }
                            }
                            token = access
                        } else {
                            arkeCollaborationRequest("/rest/v1/shared_trip_pins", session, org.json.JSONObject()
                                .put("trip_id", requireNotNull(destination)).put("title", name)
                                .put("latitude", pin.latitude).put("longitude", pin.longitude))
                            published = true
                            message = "Pin compartilhado."
                        }
                    }
                },
            ) { Text(if (session == null) "Entrar" else "Confirmar compartilhamento") }
        },
        dismissButton = { TextButton(onClick = onClose, enabled = !busy) { Text("Fechar") } },
    )
}

private suspend fun arkeCollaborationRequest(
    path: String,
    token: String? = null,
    body: org.json.JSONObject? = null,
    representation: Boolean = false,
): String = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
    val connection = java.net.URL("https://nbfvflfjegiufbtyjuch.supabase.co" + path)
        .openConnection() as java.net.HttpURLConnection
    try {
        connection.instanceFollowRedirects = false
        connection.connectTimeout = 15_000
        connection.readTimeout = 15_000
        connection.setRequestProperty("apikey", "sb_publishable_KN5XnD4anPn4_vANwr_-Fg_4_UZIsGB")
        token?.let { connection.setRequestProperty("Authorization", "Bearer $it") }
        if (representation) connection.setRequestProperty("Prefer", "return=representation")
        if (body != null) {
            connection.requestMethod = "POST"
            connection.doOutput = true
            connection.setRequestProperty("Content-Type", "application/json")
            connection.outputStream.use { it.write(body.toString().toByteArray(Charsets.UTF_8)) }
        }
        check(connection.responseCode in 200..299)
        connection.inputStream.bufferedReader().use { it.readText() }
    } finally { connection.disconnect() }
}

private val HomeStateSaver = Saver<HomeState, List<String>>(
    save = { listOf(it.context.name, it.detent.name) },
    restore = {
        HomeState(TravelContext.valueOf(it[0]), PanelDetent.valueOf(it[1]))
    },
)

@Composable
fun ArkeHome(places: PlaceIdeasViewModel, captureDetails: Boolean = false, captureEdit: Boolean = false) {
    val placeState by places.state.collectAsState()
    var savedNotice by rememberSaveable { mutableStateOf(false) }
    var pendingSaveId by rememberSaveable { mutableStateOf<String?>(null) }
    var state by rememberSaveable(stateSaver = HomeStateSaver) { mutableStateOf(HomeState()) }
    var showingTrips by rememberSaveable { mutableStateOf(false) }
    var pinToShare by remember { mutableStateOf<Pair<String, LocalMapPin>?>(null) }
    var tripDetailsID by rememberSaveable { mutableStateOf<String?>(null) }
    var pinPlan by remember { mutableStateOf<LocalPinPlan?>(null) }
    var revealedPin by remember { mutableStateOf<LocalMapPin?>(null) }
    var pinIdea by remember { mutableStateOf<PlaceIdea?>(null) }
    LaunchedEffect(places) {
        try { pinPlan = places.loadPinPlan() }
        catch (error: Exception) { if (error is kotlinx.coroutines.CancellationException) throw error }
    }
    var focusedIdeaID by remember { mutableStateOf<String?>(null) }
    var mapExpanded by remember { mutableStateOf(false) }
    var showingSavedDetails by remember { mutableStateOf(false) }
    var showingAddPlace by rememberSaveable { mutableStateOf(false) }
    var showingDetailPreview by rememberSaveable { mutableStateOf(captureDetails) }
    var showingEditPreview by rememberSaveable { mutableStateOf(captureEdit) }
    LaunchedEffect(captureDetails) {
        if (BuildConfig.DEBUG && captureDetails) showingDetailPreview = true
    }
    LaunchedEffect(placeState, pendingSaveId) {
        if (pendingSaveId != null && placeState.ready && !placeState.busy && placeState.error == null && placeState.ideas.any { it.id == pendingSaveId }) {
            showingAddPlace = false
            savedNotice = true
            pendingSaveId = null
        }
    }
    LaunchedEffect(savedNotice) { if (savedNotice) { kotlinx.coroutines.delay(4_000); savedNotice = false } }
    val modalVisible = showingAddPlace || showingDetailPreview || showingSavedDetails || showingEditPreview
    val surfaceScale by animateFloatAsState(if (modalVisible) 0.96f else 1f, label = "modalRecess")
    Surface(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surfaceContainerLow).graphicsLayer {
        scaleX = surfaceScale; scaleY = surfaceScale; transformOrigin = TransformOrigin(0.5f, 0f)
        shape = RoundedCornerShape(if (modalVisible) 28.dp else 0.dp); clip = modalVisible
    }, color = MaterialTheme.colorScheme.surfaceContainerLow) {
        Column(
            Modifier.safeDrawingPadding().imePadding().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(stringResource(R.string.app_name), style = MaterialTheme.typography.titleLarge)
            Text(stringResource(R.string.tagline), style = MaterialTheme.typography.headlineMedium)
            BoxWithConstraints(Modifier.fillMaxWidth().weight(1f).clipToBounds()) {
                val density = LocalDensity.current
                val minContentHeight = (160 * density.fontScale).dp
                val availableHeight = maxHeight
                fun heightFor(detent: PanelDetent) = maxOf(64.dp, minOf(availableHeight * when (detent) {
                    PanelDetent.EXPLORE -> 0.65f
                    PanelDetent.BALANCED -> 0.45f
                    PanelDetent.DETAILS -> 0.25f
                }, availableHeight - minContentHeight - 64.dp))
                var dragHeight by remember(availableHeight, density.density, density.fontScale) { mutableStateOf<Float?>(null) }
                val restingHeight = heightFor(state.detent)
                val maxMapPx = with(density) { maxOf(64.dp, availableHeight - minContentHeight - 64.dp).toPx() }
                val minMapPx = with(density) { 64.dp.toPx() }
                val restingPx = with(density) { restingHeight.toPx() }
                val position = remember { Animatable(restingPx) }
                LaunchedEffect(restingPx, dragHeight != null) {
                    if (dragHeight == null) position.animateTo(restingPx)
                }
                val mapHeight = with(density) { (dragHeight ?: position.value).toDp() }
                val mapExtension by animateDpAsState(
                    if (mapExpanded) (availableHeight - mapHeight - 52.dp).coerceAtLeast(0.dp) else 0.dp,
                    label = "mapExpansion",
                )
                val contentRecess by animateDpAsState(if (mapExpanded) 12.dp else 0.dp, label = "contentRecess")
                val dragModifier = Modifier.draggable(
                    orientation = Orientation.Vertical,
                    state = rememberDraggableState { delta ->
                        val current = dragHeight ?: position.value
                        dragHeight = (current + delta).coerceIn(minMapPx, maxMapPx)
                    },
                    onDragStarted = { dragHeight = position.value },
                    onDragStopped = {
                        val target = dragHeight
                        if (target != null) {
                            position.snapTo(target)
                            val closest = PanelDetent.entries.minBy { abs(with(density) { heightFor(it).toPx() } - target) }
                            state = state.setDetent(closest)
                        }
                        dragHeight = null
                    },
                )
                Column(
                    modifier = if (mapExpanded) Modifier.clearAndSetSemantics {} else Modifier,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Spacer(Modifier.height(mapHeight))

                    Box(Modifier.fillMaxWidth(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                        DetentMenu(state.detent, dragModifier) { state = state.setDetent(it) }
                    }

                    Card(
                        modifier = Modifier.fillMaxWidth().weight(1f).offset(y = contentRecess),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                    ) {
                        Column(Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp)) {
                            CardHeader(state.context, { showingAddPlace = true }, placeState.ready && !placeState.busy) { state = state.selectContext(it) }
                            HorizontalDivider(Modifier.padding(vertical = 12.dp))
                            Column(Modifier.weight(1f).testTag("home.contentBody").verticalScroll(rememberScrollState())) {
                                if (state.context == TravelContext.PLANNING) {
                                    TextButton(onClick = { showingTrips = true }) { Text("Viagens") }
                                }
                                if (savedNotice) Text(stringResource(R.string.saved_place_notice), modifier = Modifier.testTag("place.savedNotice"), style = MaterialTheme.typography.bodySmall)
                                if (placeState.error != null && !showingAddPlace) {
                                    Text(placeState.error.orEmpty(), color = MaterialTheme.colorScheme.error)
                                    TextButton(onClick = places::load) { Text(stringResource(R.string.retry)) }
                                }
                                if (placeState.ready) {
                                    if (state.context == TravelContext.PLANNING) PlaceIdeasContent(placeState,
                                        onUpdate = places::update, onEditCancel = places::load,
                                        onDetailsVisibilityChanged = { showingSavedDetails = it },
                                        onFocus = { focusedIdeaID = it }, onPin = { pinIdea = it }) { id -> savedNotice = false; places.remove(id) }
                                    else {
                                        Text("Em breve.")
                                        TextButton(onClick = { state = state.selectContext(TravelContext.PLANNING) }) { Text(stringResource(R.string.view_places)) }
                                    }
                                } else if (placeState.busy) Text(stringResource(R.string.loading_places))

                            }
                        }
                    }
                }
                Card(
                    modifier = Modifier.fillMaxWidth().height(mapHeight + mapExtension)
                        .border(1.dp, androidx.compose.ui.graphics.Color.White.copy(alpha = 0.42f), RoundedCornerShape(30.dp)),
                    shape = RoundedCornerShape(30.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                 ) {
                    Box(Modifier.fillMaxSize()) {
                        TravelMap(Modifier.fillMaxSize(), pins = pinPlan?.pins.orEmpty().mapNotNull { pin ->
                            placeState.ideas.firstOrNull { it.id == pin.ideaID }?.let { pin to it.name }
                        }, revealPin = revealedPin)
                        val focusedIdea = placeState.ideas.firstOrNull { it.id == focusedIdeaID }
                        if (state.context == TravelContext.PLANNING && focusedIdea != null) {
                            Surface(
                                modifier = Modifier.align(Alignment.TopStart).padding(start = 12.dp, top = 18.dp, end = 72.dp),
                                shape = RoundedCornerShape(24.dp),
                                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                            ) {
                                Text(
                                    focusedIdea.name,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    maxLines = 1,
                                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                                )
                            }
                        }
                        FilledTonalIconButton(
                            onClick = { mapExpanded = !mapExpanded },
                            modifier = Modifier.align(Alignment.TopEnd).padding(12.dp).testTag("home.expandMap"),
                        ) {
                            Text(
                                if (mapExpanded) "↘" else "↗",
                                modifier = Modifier.clearAndSetSemantics {
                                    contentDescription = if (mapExpanded) "Recolher mapa" else "Expandir mapa"
                                },
                            )
                        }
                    }
                }
            }
        }
    }
    pinToShare?.let { (name, pin) -> SharePinDialog(name, pin) { pinToShare = null } }
    if (showingTrips && pinToShare == null) {
        val plan = pinPlan
        val trip = plan?.trips?.firstOrNull { it.id == tripDetailsID }
        fun closeTrips() {
            showingTrips = false
            tripDetailsID = null
        }
        AlertDialog(
            onDismissRequest = { if (trip == null) closeTrips() else tripDetailsID = null },
            title = { Text(trip?.title ?: "Viagens") },
            text = {
                Column(Modifier.verticalScroll(rememberScrollState())) {
                    when {
                        plan == null -> Text("Não foi possível abrir as viagens.")
                        trip != null -> {
                            val ideas = placeState.ideas.filter { idea ->
                                plan.pins.any { it.ideaID == idea.id && it.tripID == trip.id }
                            }
                            if (ideas.isEmpty()) Text("Nenhum Pin nesta viagem.")
                            ideas.forEach { idea ->
                                Column(Modifier.padding(vertical = 8.dp)) {
                                    Text(idea.name, style = MaterialTheme.typography.titleMedium)
                                    Text(idea.address, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    plan.pins.firstOrNull { it.ideaID == idea.id }?.let { pin ->
                                        TextButton(onClick = { pinToShare = idea.name to pin }) { Text("Compartilhar Pin") }
                                    }
                                }
                            }
                        }
                        plan.trips.isEmpty() -> Text("Suas viagens aparecem aqui quando você vincula um Pin.")
                        else -> plan.trips.forEach { item ->
                            TextButton(onClick = { tripDetailsID = item.id }) { Text(item.title) }
                        }
                    }
                }
            },
            confirmButton = { TextButton(onClick = { closeTrips() }) { Text("Fechar") } },
            dismissButton = {
                if (trip != null) TextButton(onClick = { tripDetailsID = null }) { Text("Voltar") }
            },
        )
    }
    pinIdea?.let { idea ->
        PinEditor(idea, places, onClose = { pinIdea = null }) { plan ->
            pinPlan = plan
            revealedPin = plan.pins.firstOrNull { it.ideaID == idea.id }
            pinIdea = null
        }
    }
    if (BuildConfig.DEBUG && showingDetailPreview) {
        val examples = remember { detailCaptureIdeas() }
        PlaceDetails(examples, examples.first().id, onClose = { showingDetailPreview = false })
    }
    if (showingAddPlace) {
        PlaceEditor(saving = placeState.busy, saveError = placeState.error,
            onSave = { draft -> pendingSaveId = draft.id; places.add(draft) },
            onCancel = { showingAddPlace = false; pendingSaveId = null })
    }
    if (BuildConfig.DEBUG && showingEditPreview) {
        val example = remember { detailCaptureIdeas().first() }
        PlaceEditor(initialIdea = example, saving = false, saveError = null,
            onSave = {}, onCancel = { showingEditPreview = false })
    }
}

@Composable
private fun CardHeader(context: TravelContext, onAdd: () -> Unit, addEnabled: Boolean, onSelect: (TravelContext) -> Unit) {
    val actionColor = if (isSystemInDarkTheme()) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.primary
    val view = androidx.compose.ui.platform.LocalView.current
    Box(Modifier.fillMaxWidth(), contentAlignment = androidx.compose.ui.Alignment.CenterStart) {
        val options = TravelContext.entries
        val index = options.indexOf(context)
        var drag by remember { mutableFloatStateOf(0f) }
        val threshold = with(LocalDensity.current) { 24.dp.toPx() }
        Row(
            Modifier.fillMaxWidth().heightIn(min = 48.dp).draggable(
                orientation = Orientation.Horizontal,
                state = rememberDraggableState { drag += it },
                onDragStarted = { drag = 0f },
                onDragStopped = {
                    if (abs(drag) >= threshold) {
                        options.getOrNull(index + if (drag < 0) 1 else -1)?.let {
                            view.performHapticFeedback(android.view.HapticFeedbackConstants.CLOCK_TICK)
                            onSelect(it)
                        }
                    }
                },
            ),
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
        ) {
            ContextPreview(options.getOrNull(index - 1))
            Text(
                stringResource(context.title()),
                modifier = Modifier.weight(1f).testTag("home.contextTitle").semantics {
                    heading()
                    customActions = listOfNotNull(
                        options.getOrNull(index - 1)?.let { previous ->
                            CustomAccessibilityAction("Contexto anterior") { onSelect(previous); true }
                        },
                        options.getOrNull(index + 1)?.let { next ->
                            CustomAccessibilityAction("Próximo contexto") { onSelect(next); true }
                        },
                    )
                },
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
            )
            ContextPreview(options.getOrNull(index + 1))
        }
        IconButton(onClick = onAdd, enabled = addEnabled, modifier = Modifier.size(48.dp).background(arkeInsetColor(), androidx.compose.foundation.shape.CircleShape).testTag("home.addPlace")) {
            Icon(painterResource(R.drawable.ic_add), contentDescription = stringResource(R.string.add_place), tint = actionColor)
        }
    }
}

@Composable
private fun ContextPreview(option: TravelContext?) {
    Box(Modifier.width(56.dp)) {
        if (option != null) {
            Text(stringResource(option.title()), maxLines = 1, softWrap = false,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.34f),
                modifier = Modifier.clearAndSetSemantics {})
        }
    }
}

@Composable
private fun DetentMenu(selected: PanelDetent, modifier: Modifier, onSelect: (PanelDetent) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val description = stringResource(R.string.change_panel, stringResource(selected.title()))
    val moreMap = stringResource(R.string.more_map)
    val moreContent = stringResource(R.string.more_content)
    Box {
        TextButton(
            onClick = { expanded = true },
            colors = ButtonDefaults.textButtonColors(
                contentColor = if (isSystemInDarkTheme()) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.primary,
            ),
            modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp).then(modifier).testTag("home.panelMenu").semantics {
                contentDescription = description
                customActions = listOf(
                    CustomAccessibilityAction(moreMap) { onSelect(PanelDetent.entries[maxOf(0, selected.ordinal - 1)]); true },
                    CustomAccessibilityAction(moreContent) { onSelect(PanelDetent.entries[minOf(2, selected.ordinal + 1)]); true },
                )
            },
        ) {
            Column(horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Box(Modifier.size(width = 36.dp, height = 4.dp).background(MaterialTheme.colorScheme.onSurfaceVariant, RoundedCornerShape(2.dp)))
                Text(stringResource(selected.title()), style = MaterialTheme.typography.labelMedium)
            }
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            PanelDetent.entries.forEach { detent ->
                DropdownMenuItem(
                    modifier = Modifier.testTag("detent.${detent.name}"),
                    text = { Text(stringResource(detent.title())) },
                    onClick = { onSelect(detent); expanded = false },
                )
            }
        }
    }
}

@StringRes private fun TravelContext.title(): Int = when (this) {
    TravelContext.PLANNING -> R.string.planning
    TravelContext.EXPENSES -> R.string.expenses
    TravelContext.SAVINGS -> R.string.savings
    TravelContext.REVIEWS -> R.string.reviews
    TravelContext.MEMORIES -> R.string.memories
}

@StringRes private fun PanelDetent.title(): Int = when (this) {
    PanelDetent.EXPLORE -> R.string.explore
    PanelDetent.BALANCED -> R.string.balanced
    PanelDetent.DETAILS -> R.string.details
}
