package dev.arke.app

import androidx.annotation.StringRes
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalFocusManager
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

/** Compartilhar exige conta (D5 pendente, GROUP-001). Sem credencial, URL de servidor ou login no código: nada é enviado. */
@Composable
private fun SharePinDialog(name: String, @Suppress("UNUSED_PARAMETER") pin: LocalMapPin, onClose: () -> Unit) {
    AlertDialog(
        onDismissRequest = onClose,
        title = { Text("Compartilhar exige uma conta Arke") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("O compartilhamento de viagens ainda não está disponível nesta versão. $name continua salvo só neste aparelho e nada é enviado.",
                    modifier = Modifier.testTag("share.unavailable"))
                Text("O Cofrinho continua só seu.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        },
        confirmButton = { TextButton(onClick = onClose) { Text("Fechar") } },
    )
}

private val HomeStateSaver = Saver<HomeState, List<String>>(
    save = { listOf(it.context.name, it.detent.name) },
    restore = {
        HomeState(TravelContext.valueOf(it[0]), PanelDetent.valueOf(it[1]))
    },
)

@Composable
fun ArkeHome(places: PlaceIdeasViewModel, captureDetails: Boolean = false, captureEdit: Boolean = false, journalCapture: String? = null) {
    val placeState by places.state.collectAsState()
    var savedNotice by rememberSaveable { mutableStateOf(false) }
    var pendingSaveId by rememberSaveable { mutableStateOf<String?>(null) }
    val journalMode = journalCapture?.split("/")
    var state by rememberSaveable(journalCapture, stateSaver = HomeStateSaver) {
        val initial = runCatching {
            HomeState(TravelContext.valueOf(journalMode.orEmpty()[0].uppercase()), PanelDetent.valueOf(journalMode.orEmpty()[1].uppercase()))
        }.getOrDefault(HomeState())
        mutableStateOf(initial)
    }
    var showingTrips by rememberSaveable { mutableStateOf(false) }
    var mapDetailIdeaID by rememberSaveable { mutableStateOf<String?>(null) }
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
            BoxWithConstraints(Modifier.fillMaxWidth().weight(1f).clipToBounds()) {
                val density = LocalDensity.current
                val keyboardVisible = WindowInsets.ime.getBottom(density) > 0
                val minContentHeight = (160 * density.fontScale).dp
                val availableHeight = maxHeight
                fun heightFor(detent: PanelDetent) = maxOf(minOf(84.dp, availableHeight), minOf(availableHeight * when (detent) {
                    PanelDetent.EXPLORE -> 0.65f
                    PanelDetent.BALANCED -> 0.45f
                    PanelDetent.DETAILS -> 0.25f
                }, availableHeight - minContentHeight - 64.dp))
                var dragHeight by remember(availableHeight, density.density, density.fontScale) { mutableStateOf<Float?>(null) }
                val restingHeight = heightFor(state.detent)
                val maxMapPx = with(density) { maxOf(minOf(84.dp, availableHeight), availableHeight - minContentHeight - 64.dp).toPx() }
                val minMapPx = with(density) { minOf(84.dp, availableHeight).toPx() }
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
                // Com o mapa expandido só a borda do cartão fica à mostra: sem texto sob a alça.
                val peekSurface = MaterialTheme.colorScheme.surface
                val dragModifier = Modifier.draggable(
                    orientation = Orientation.Vertical,
                    enabled = !keyboardVisible,
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
                        modifier = Modifier.fillMaxWidth().weight(1f).offset(y = contentRecess).drawWithContent {
                            drawContent()
                            if (mapExpanded) drawRect(peekSurface)
                        },
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                    ) {
                        var headerHeight by remember { mutableIntStateOf(0) }
                        val scroll = rememberScrollState()
                        Box(Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
                            Column(Modifier.fillMaxSize().testTag("home.contentBody").verticalScroll(scroll)
                                .padding(top = with(density) { headerHeight.toDp() })) {
                                if (state.context == TravelContext.PLANNING) {
                                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween) {
                                        TextButton(onClick = { showingTrips = true },
                                            modifier = Modifier.heightIn(min = 48.dp).testTag("home.trips"),
                                            colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.onSurface)) {
                                            Icon(painterResource(R.drawable.ic_suitcase), null, Modifier.size(18.dp))
                                            Spacer(Modifier.width(6.dp))
                                            Text("Viagens")
                                        }
                                        if (placeState.ready) Text(
                                            if (placeState.ideas.size == 1) "1 lugar" else "${placeState.ideas.size} lugares",
                                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
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
                                        onFocus = { focusedIdeaID = it }, onPin = { pinIdea = it }, detent = state.detent) { id -> savedNotice = false; places.remove(id) }
                                    else {
                                        if (state.context == TravelContext.EXPENSES || state.context == TravelContext.SAVINGS) {
                                            if (state.detent == PanelDetent.EXPLORE) {
                                                TextButton(onClick = { state = state.setDetent(PanelDetent.DETAILS) }) {
                                                    Text(stringResource(state.context.title()))
                                                }
                                            } else {
                                                LocalFinanceContent(pinPlan?.trips.orEmpty(), savings = state.context == TravelContext.SAVINGS,
                                                    detailed = state.detent == PanelDetent.DETAILS)
                                            }
                                        } else {
                                            if (state.detent == PanelDetent.EXPLORE) {
                                                TextButton(onClick = { state = state.setDetent(PanelDetent.DETAILS) }) {
                                                    Text(stringResource(state.context.title()))
                                                }
                                            } else {
                                                key(state.context) {
                                                    TripJournalContent(pinPlan, placeState.ideas, state.context == TravelContext.REVIEWS,
                                                        detailed = state.detent == PanelDetent.DETAILS, preview = journalCapture != null)
                                                }
                                                if (state.detent == PanelDetent.BALANCED) {
                                                    TextButton(onClick = { state = state.setDetent(PanelDetent.DETAILS) }) { Text("Ver detalhes") }
                                                }
                                            }
                                        }
                                        TextButton(onClick = { state = state.selectContext(TravelContext.PLANNING) }) { Text(stringResource(R.string.view_places)) }
                                    }
                                } else if (placeState.busy) Text(stringResource(R.string.loading_places))

                            }
                            val headerColor = MaterialTheme.colorScheme.surface
                            Column(Modifier.fillMaxWidth().onSizeChanged { headerHeight = it.height }
                                .background(Brush.verticalGradient(listOf(headerColor, headerColor,
                                    if (scroll.value > 0) Color.Transparent else headerColor)))
                                .padding(top = 16.dp, bottom = 10.dp)) {
                                CardHeader(state.context, { showingAddPlace = true }, placeState.ready && !placeState.busy) { state = state.selectContext(it) }
                                if (state.context == TravelContext.PLANNING && state.detent != PanelDetent.EXPLORE) {
                                    placeState.ideas.firstOrNull { it.id == focusedIdeaID }?.let { idea ->
                                        Text(idea.name, style = MaterialTheme.typography.labelMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1,
                                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
                                    }
                                }
                            }
                        }
                    }
                }
                Card(
                    modifier = Modifier.fillMaxWidth().height(mapHeight + mapExtension),
                    shape = RoundedCornerShape(30.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                 ) {
                    Box(Modifier.fillMaxSize().drawWithContent {
                        drawContent()
                        for (width in 2..14 step 2) {
                            drawRoundRect(
                                color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.025f),
                                cornerRadius = androidx.compose.ui.geometry.CornerRadius(30.dp.toPx()),
                                style = androidx.compose.ui.graphics.drawscope.Stroke(width.dp.toPx()),
                            )
                        }
                    }) {
                        TravelMap(Modifier.fillMaxSize(), pins = pinPlan?.pins.orEmpty().mapNotNull { pin ->
                            placeState.ideas.firstOrNull { it.id == pin.ideaID }?.let { pin to it.name }
                        }, revealPin = revealedPin, onPinSelected = { pin ->
                            focusedIdeaID = pin.ideaID
                            if (pin.tripID != null) {
                                tripDetailsID = pin.tripID
                                showingTrips = true
                            } else {
                                mapDetailIdeaID = pin.ideaID
                            }
                        })
                        val focusedIdea = placeState.ideas.firstOrNull { it.id == focusedIdeaID }
                        if (mapHeight + mapExtension >= 150.dp && state.context == TravelContext.PLANNING && focusedIdea != null) {
                            Surface(
                                modifier = Modifier.align(Alignment.BottomStart).padding(start = 12.dp, bottom = 18.dp, end = 72.dp),
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
                            enabled = !keyboardVisible,
                            modifier = Modifier.align(Alignment.TopEnd).padding(12.dp).size(40.dp).testTag("home.expandMap"),
                        ) {
                            val toggleLabel = if (mapExpanded) "Recolher mapa" else "Expandir mapa"
                            Icon(painterResource(R.drawable.ic_expand), toggleLabel, Modifier.size(22.dp))
                        }
                    }
                }
                if (mapExpanded) {
                    var closeDistance by remember { mutableStateOf(0f) }
                    val closeThreshold = with(density) { 16.dp.toPx() }
                    Box(
                        Modifier.align(Alignment.BottomCenter).fillMaxWidth().height(48.dp)
                            .draggable(rememberDraggableState { closeDistance += it }, Orientation.Vertical,
                                onDragStarted = { closeDistance = 0f },
                                // Mesma direção da alça normal: arrastar para cima devolve espaço ao cartão.
                                onDragStopped = { if (closeDistance < -closeThreshold) mapExpanded = false })
                            .clickable { mapExpanded = false }
                            .semantics {
                                contentDescription = "Recolher mapa"
                                customActions = listOf(CustomAccessibilityAction("Recolher mapa") { mapExpanded = false; true })
                            },
                        contentAlignment = Alignment.Center,
                    ) { Box(Modifier.size(36.dp, 4.dp).background(MaterialTheme.colorScheme.onSurfaceVariant, RoundedCornerShape(2.dp))) }
                }
            }
        }
    }
    mapDetailIdeaID?.let { id -> PlaceDetails(placeState.ideas, id) { mapDetailIdeaID = null } }
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
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        val options = TravelContext.entries
        val index = options.indexOf(context)
        var drag by remember { mutableFloatStateOf(0f) }
        val threshold = with(LocalDensity.current) { 24.dp.toPx() }
        Row(
            Modifier.weight(1f).heightIn(min = 48.dp).draggable(
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
            ContextNeighbor(options.getOrNull(index - 1), forward = false, onSelect = onSelect)
            Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
            Icon(painterResource(context.icon()), null, Modifier.size(18.dp), tint = actionColor)
            Spacer(Modifier.width(8.dp))
            Text(
                stringResource(context.title()),
                modifier = Modifier.testTag("home.contextTitle").semantics {
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
            }
            ContextDots(index, options.size)
            }
            ContextNeighbor(options.getOrNull(index + 1), forward = true, onSelect = onSelect)
        }
        IconButton(onClick = onAdd, enabled = addEnabled,
            modifier = Modifier.size(44.dp).background(ArkeColors.brand, androidx.compose.foundation.shape.CircleShape).testTag("home.addPlace")) {
            Icon(painterResource(R.drawable.ic_add), contentDescription = stringResource(R.string.add_place), tint = androidx.compose.ui.graphics.Color.White)
        }
    }
}

/** Vizinho do contexto atual: botão tocável, nunca texto cortado. O nome completo vai para a acessibilidade. */
@Composable
private fun ContextNeighbor(option: TravelContext?, forward: Boolean, onSelect: (TravelContext) -> Unit) {
    if (option == null) {
        Spacer(Modifier.size(44.dp, 48.dp))
        return
    }
    val label = (if (forward) "Próximo contexto: " else "Contexto anterior: ") + stringResource(option.title())
    IconButton(
        onClick = { onSelect(option) },
        modifier = Modifier.size(44.dp, 48.dp).testTag(if (forward) "home.contextNext" else "home.contextPrevious"),
    ) {
        Icon(painterResource(option.icon()), contentDescription = label,
            tint = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

/** Posição entre os cinco contextos, só como apoio visual (o seletor anuncia a posição por semântica). */
@Composable
private fun ContextDots(index: Int, count: Int) {
    Row(Modifier.padding(top = 4.dp).clearAndSetSemantics {}, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
        repeat(count) { position ->
            val selected = position == index
            Box(Modifier.size(if (selected) 16.dp else 6.dp, 6.dp).background(
                if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f),
                RoundedCornerShape(3.dp)))
        }
    }
}

@Composable
private fun DetentMenu(selected: PanelDetent, modifier: Modifier, onSelect: (PanelDetent) -> Unit) {
    val description = stringResource(R.string.change_panel, stringResource(selected.title()))
    val moreMap = stringResource(R.string.more_map)
    val moreContent = stringResource(R.string.more_content)
    Box(
        Modifier.fillMaxWidth().heightIn(min = 48.dp).then(modifier).testTag("home.panelMenu").semantics {
            contentDescription = description
            customActions = listOf(
                CustomAccessibilityAction(moreMap) { onSelect(PanelDetent.entries[maxOf(0, selected.ordinal - 1)]); true },
                CustomAccessibilityAction(moreContent) { onSelect(PanelDetent.entries[minOf(2, selected.ordinal + 1)]); true },
            )
        },
        contentAlignment = Alignment.Center,
    ) { Box(Modifier.size(36.dp, 4.dp).background(MaterialTheme.colorScheme.onSurfaceVariant, RoundedCornerShape(2.dp))) }
}

@StringRes private fun TravelContext.title(): Int = when (this) {
    TravelContext.PLANNING -> R.string.planning
    TravelContext.EXPENSES -> R.string.expenses
    TravelContext.SAVINGS -> R.string.savings
    TravelContext.REVIEWS -> R.string.reviews
    TravelContext.MEMORIES -> R.string.memories
}


@Composable
private fun TripJournalContent(plan: LocalPinPlan?, ideas: List<PlaceIdea>, reviews: Boolean, detailed: Boolean, preview: Boolean = false) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val prefs = remember(context) { context.getSharedPreferences("arke.trip-journal.v1", 0) }
    var raw by remember(prefs) { mutableStateOf(prefs.getString("records", "[]") ?: "[]") }
    var tripID by remember { mutableStateOf<String?>(null) }
    val activePlan = if (preview) LocalPinPlan(emptyList(), listOf(LocalPinTrip("preview", "Milano"))) else plan
    val trip = activePlan?.trips?.firstOrNull { it.id == tripID } ?: activePlan?.trips?.firstOrNull()
    if (activePlan == null || trip == null) {
        Text("Crie uma viagem local para começar.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        return
    }
    var draft by remember(trip.id, reviews) { mutableStateOf("") }
    var rating by remember(trip.id, reviews) { mutableIntStateOf(5) }
    var placeID by remember(trip.id, reviews) { mutableStateOf("") }
    var failed by remember(trip.id, reviews) { mutableStateOf(false) }
    val records = remember(raw) {
        runCatching {
            val array = org.json.JSONArray(raw)
            (0 until array.length()).map { array.getJSONObject(it) }
        }.getOrNull()
    }
    if (records == null) {
        Text("Não foi possível abrir os registros.")
        return
    }
    FinanceChoice("Viagem", trip.title, activePlan.trips.map { it.id to it.title }) { tripID = it }
    val source = if (preview) listOf(org.json.JSONObject().put("trip", "preview")
        .put("text", if (reviews) "Chegamos cedo e encontramos a praça tranquila." else "A primeira manhã na praça do Duomo.")
        .put("rating", if (reviews) 4 else 0)) else records
    val entries = source.filter { it.optString("trip") == trip.id && (it.optInt("rating") > 0) == reviews }
    if (entries.isEmpty()) Text(if (reviews) "Nenhuma avaliação nesta viagem." else "Nenhuma memória nesta viagem.")
    val visible = if (detailed) entries.asReversed() else entries.takeLast(1)
    visible.forEach { entry ->
        if (!reviews && !detailed) {
            Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                JournalMemoryCover(ideas, entry.optString("place"), preview,
                    Modifier.size(width = 64.dp, height = 48.dp))
                Text(entry.getString("text"), maxLines = 2, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
            }
        } else Column(Modifier.padding(vertical = 8.dp)) {
            if (reviews) {
                Text("★ ${entry.getInt("rating")} de 5", style = MaterialTheme.typography.titleMedium)
            } else {
                JournalMemoryCover(ideas, entry.optString("place"), preview, Modifier.fillMaxWidth().height(190.dp))
            }
            Text(entry.getString("text"), maxLines = if (detailed) Int.MAX_VALUE else 2,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
        }
    }
    if (detailed) {
        if (reviews) {
            FinanceChoice("Nota", "$rating de 5", (1..5).map { it.toString() to "$it de 5" }) { rating = it.toInt() }
        } else {
            val covers = ideas.filter { idea ->
                idea.cover.isNotEmpty() && activePlan.pins.any { it.ideaID == idea.id && it.tripID == trip.id }
            }
            FinanceChoice("Capa", covers.firstOrNull { it.id == placeID }?.name ?: "Sem capa",
                listOf("" to "Sem capa") + covers.map { it.id to it.name }) { placeID = it }
        }
        OutlinedTextField(draft, { draft = it }, label = { Text(if (reviews) "Escreva sua avaliação" else "Escreva sua memória") })
        TextButton(enabled = !preview && draft.isNotBlank() && draft.length <= 1000, onClick = {
            val entry = org.json.JSONObject().put("trip", trip.id).put("text", draft.trim())
            if (reviews) entry.put("rating", rating) else entry.put("place", placeID)
            val updated = org.json.JSONArray(records + entry).toString()
            if (prefs.edit().putString("records", updated).commit()) {
                raw = updated
                draft = ""
                placeID = ""
                failed = false
            } else failed = true
        }) { Text(if (reviews) "Salvar avaliação" else "Salvar memória") }
        if (failed) Text("Não foi possível salvar.", color = MaterialTheme.colorScheme.error)
    }
}

@Composable
private fun JournalMemoryCover(ideas: List<PlaceIdea>, placeID: String, preview: Boolean, modifier: Modifier) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val cover = ideas.firstOrNull { it.id == placeID }?.cover
    val image = remember(cover, preview) {
        if (preview) android.graphics.BitmapFactory.decodeResource(context.resources, R.mipmap.ic_launcher_arke)
        else cover?.let { decodePlaceCover(it) }
    }
    if (image != null) {
        androidx.compose.foundation.Image(image.asImageBitmap(), null,
            modifier = modifier.arkeStamp(MaterialTheme.colorScheme.surface).padding(6.dp),
            contentScale = androidx.compose.ui.layout.ContentScale.Fit)
    } else Text("Memória", style = MaterialTheme.typography.labelMedium)
}

@Composable
private fun LocalFinanceContent(trips: List<LocalPinTrip>, savings: Boolean, detailed: Boolean) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val financeFocus = LocalFocusManager.current
    val prefs = remember(context) { context.getSharedPreferences("arke.local-finance.v1", 0) }
    var raw by remember(prefs) { mutableStateOf(prefs.getString("records", "[]") ?: "[]") }
    var tripID by remember { mutableStateOf<String?>(null) }
    val trip = trips.firstOrNull { it.id == tripID } ?: trips.firstOrNull()
    if (trip == null) {
        Text("Crie uma viagem local para começar.")
        return
    }
    var kind by remember(trip.id, savings) { mutableStateOf("") }
    var name by remember(trip.id, savings) { mutableStateOf("") }
    var currency by remember(trip.id, savings) { mutableStateOf("BRL") }
    var amount by remember(trip.id, savings) { mutableStateOf("") }
    var editing by remember(trip.id, savings) { mutableStateOf<String?>(null) }
    var error by remember(trip.id, savings) { mutableStateOf<String?>(null) }
    val records = remember(raw) {
        runCatching {
            val array = org.json.JSONArray(raw)
            (0 until array.length()).map { array.getJSONObject(it) }
        }.getOrNull()
    }
    if (records == null) {
        Text("Não foi possível abrir os registros.")
        return
    }

    fun digits(code: String) = java.util.Currency.getInstance(code).defaultFractionDigits
    fun display(value: Long, code: String): String = java.text.NumberFormat.getNumberInstance().apply {
        isGroupingUsed = false
        maximumFractionDigits = digits(code)
    }.format(java.math.BigDecimal.valueOf(value, digits(code)))
    fun formatted(value: Long, code: String) = "$code ${display(value, code)}"
    fun clearDraft() { editing = null; kind = ""; name = ""; amount = ""; error = null }

    FinanceChoice("Viagem", trip.title, trips.map { it.id to it.title }) { tripID = it }
    val acceptedKinds = if (savings) setOf("goal", "saving") else setOf("planned", "incurred")
    val entries = records.filter { it.optString("trip") == trip.id && it.optString("kind") in acceptedKinds }
    val goal = entries.firstOrNull { it.getString("kind") == "goal" }
    val goalCurrency = goal?.getString("currency")
    val contributions = entries.filter { it.getString("kind") == "saving" }
    if (savings && goal != null && goalCurrency != null) {
        val total = contributions.filter { it.getString("currency") == goalCurrency }.sumOf { it.getLong("amount") }
        Text("Reservado: ${formatted(total, goalCurrency)} / ${formatted(goal.getLong("amount"), goalCurrency)}")
        LinearProgressIndicator(progress = { (total.toDouble() / goal.getLong("amount")).toFloat().coerceIn(0f, 1f) })
        Text("Falta: ${formatted((goal.getLong("amount") - total).coerceAtLeast(0), goalCurrency)}")
    }
    if (!savings) {
        entries.groupBy { it.getString("currency") }.toSortedMap().forEach { (code, subtotal) ->
            Text("Previsto: ${formatted(subtotal.filter { it.getString("kind") == "planned" }.sumOf { it.getLong("amount") }, code)}")
            Text("Realizado: ${formatted(subtotal.filter { it.getString("kind") == "incurred" }.sumOf { it.getLong("amount") }, code)}")
        }
    }
    (if (detailed) entries else entries.takeLast(3)).forEach { entry ->
        val code = entry.getString("currency")
        TextButton(onClick = {
            editing = entry.getString("id")
            kind = entry.getString("kind")
            name = entry.getString("name")
            currency = code
            amount = display(entry.getLong("amount"), code)
            error = null
        }) { Text("${entry.getString("name")} · ${formatted(entry.getLong("amount"), code)}") }
    }
    val types = if (savings) {
        if (goal == null) listOf("goal" to "Meta") else listOf("goal" to "Meta", "saving" to "Valor reservado")
    } else listOf("planned" to "Prevista", "incurred" to "Realizada")
    FinanceChoice("Tipo", types.firstOrNull { it.first == kind }?.second.orEmpty(), types, !savings || editing == null) { kind = it }
    if (!savings) OutlinedTextField(name, { name = it }, label = { Text("Descrição") })
    val code = if (savings && (kind == "saving" || contributions.isNotEmpty())) goalCurrency ?: currency else currency
    FinanceChoice("Moeda", code, listOf("BRL", "USD", "EUR", "JPY", "KWD").map { it to it },
        !savings || (kind != "saving" && contributions.isEmpty())) { currency = it }
    OutlinedTextField(amount, { amount = it }, label = { Text("Valor") },
        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal))
    if (WindowInsets.ime.getBottom(LocalDensity.current) > 0) {
        TextButton(onClick = { financeFocus.clearFocus() }) { Text("Fechar teclado") }
    }
    error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
    TextButton(onClick = {
        val separator = java.text.DecimalFormatSymbols.getInstance().decimalSeparator
        val pieces = amount.trim().split(separator)
        val value = pieces.joinToString(".").toBigDecimalOrNull()
        val valid = pieces.size <= 2 && pieces.all { it.isNotEmpty() && it.all { digit -> digit in '0'..'9' } } &&
            (pieces.getOrNull(1)?.length ?: 0) <= digits(code) && value != null &&
            value > java.math.BigDecimal.ZERO && value <= java.math.BigDecimal("1000000000")
        if (!valid || kind.isEmpty() || (!savings && name.isBlank()) || (kind == "saving" && goal == null)) {
            error = "Informe um tipo e um valor válido para a moeda."
            return@TextButton
        }
        val existing = editing ?: if (kind == "goal") goal?.getString("id") else null
        if (existing == null && records.size >= 10_000) {
            error = "Limite de registros atingido."
            return@TextButton
        }
        val id = existing ?: java.util.UUID.randomUUID().toString()
        val label = if (!savings) name.trim() else if (kind == "goal") "Meta" else "Valor reservado"
        val record = org.json.JSONObject().put("id", id).put("trip", trip.id).put("name", label)
            .put("currency", code).put("kind", kind).put("amount", requireNotNull(value).movePointRight(digits(code)).longValueExact())
        val updated = records.toMutableList()
        val index = updated.indexOfFirst { it.optString("id") == id }
        if (index >= 0) updated[index] = record else updated.add(record)
        val encoded = org.json.JSONArray(updated).toString()
        if (prefs.edit().putString("records", encoded).commit()) {
            raw = encoded
            financeFocus.clearFocus()
            clearDraft()
        } else {
            error = "Não foi possível salvar os registros."
        }
    }) { Text(if (editing == null) "Salvar registro" else "Salvar alterações") }
    if (editing != null) TextButton(onClick = { clearDraft() }) { Text("Cancelar edição") }
}

@Composable
private fun FinanceChoice(label: String, selection: String, choices: List<Pair<String, String>>, enabled: Boolean = true, onSelect: (String) -> Unit) {
    var open by remember { mutableStateOf(false) }
    Box {
        TextButton(onClick = { open = true }, enabled = enabled) {
            Text(if (selection.isEmpty()) label else "$label: $selection")
        }
        DropdownMenu(expanded = open, onDismissRequest = { open = false }) {
            choices.forEach { (id, title) ->
                DropdownMenuItem(text = { Text(title) }, onClick = { open = false; onSelect(id) })
            }
        }
    }
}

@StringRes private fun PanelDetent.title(): Int = when (this) {
    PanelDetent.EXPLORE -> R.string.explore
    PanelDetent.BALANCED -> R.string.balanced
    PanelDetent.DETAILS -> R.string.details
}


/** Ícone por contexto (D6). */
private fun TravelContext.icon(): Int = when (this) {
    TravelContext.PLANNING -> R.drawable.ic_pin
    TravelContext.EXPENSES -> R.drawable.ic_receipt
    TravelContext.SAVINGS -> R.drawable.ic_target
    TravelContext.REVIEWS -> R.drawable.ic_star
    TravelContext.MEMORIES -> R.drawable.ic_photo
}
