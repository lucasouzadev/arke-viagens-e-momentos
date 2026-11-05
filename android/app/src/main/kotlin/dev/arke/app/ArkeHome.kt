package dev.arke.app

import androidx.annotation.StringRes
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.border
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
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

@OptIn(ExperimentalMaterial3Api::class)
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
    mapDetailIdeaID?.let { id -> PlaceDetails(placeState.ideas, id, onClose = { mapDetailIdeaID = null }) }
    pinToShare?.let { (name, pin) -> SharePinDialog(name, pin) { pinToShare = null } }
    if (showingTrips && pinToShare == null) {
        val plan = pinPlan
        val trip = plan?.trips?.firstOrNull { it.id == tripDetailsID }
        fun closeTrips() {
            showingTrips = false
            tripDetailsID = null
        }
        // Sheet contínuo: título rola com o conteúdo; fechar é arrastar para baixo (ou Voltar).
        ArkeFloatingSheet(onDismiss = { if (trip == null) closeTrips() else tripDetailsID = null }, tagPrefix = "trips",
            modifier = Modifier.fillMaxHeight(0.8f)) {
            Column(Modifier.fillMaxWidth().weight(1f).verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp).padding(top = 28.dp, bottom = 32.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(trip?.title ?: "Viagens", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold,
                    modifier = Modifier.semantics { heading() })
                @Composable
                fun TripRow(iconRes: Int, title: String, detail: String, onClick: (() -> Unit)?) {
                    Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(26.dp)).background(arkeInsetColor())
                        .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                        Box(Modifier.size(48.dp).clip(CircleShape).background(arkeSheetColor()), contentAlignment = Alignment.Center) {
                            Icon(painterResource(iconRes), null, Modifier.size(22.dp))
                        }
                        Column(Modifier.weight(1f)) {
                            Text(title, style = MaterialTheme.typography.titleMedium, maxLines = 1,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
                            Text(detail, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
                when {
                    plan == null -> Text("Não foi possível abrir as viagens.")
                    trip != null -> {
                        val ideas = placeState.ideas.filter { idea ->
                            plan.pins.any { it.ideaID == idea.id && it.tripID == trip.id }
                        }
                        if (ideas.isEmpty()) Text("Nenhum Pin nesta viagem.")
                        ideas.forEach { idea ->
                            Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(22.dp)).background(arkeInsetColor()).padding(16.dp)) {
                                Text(idea.name, style = MaterialTheme.typography.titleMedium)
                                Text(idea.address, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                plan.pins.firstOrNull { it.ideaID == idea.id }?.let { pin ->
                                    TextButton(onClick = { pinToShare = idea.name to pin }) { Text("Compartilhar Pin") }
                                }
                            }
                        }
                        TextButton(onClick = { tripDetailsID = null }, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) { Text("Voltar") }
                    }
                    else -> {
                        if (plan.trips.isEmpty()) Text("Suas viagens aparecem aqui quando você vincula um Pin.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                        plan.trips.forEach { item ->
                            val count = plan.pins.count { it.tripID == item.id }
                            TripRow(R.drawable.ic_suitcase, item.title, if (count == 1) "1 lugar" else "$count lugares") { tripDetailsID = item.id }
                        }
                        Text("Fora de viagens", style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 10.dp, start = 4.dp))
                        val loose = placeState.ideas.count { idea -> plan.pins.none { it.ideaID == idea.id && it.tripID != null } }
                        TripRow(R.drawable.ic_pin, "Ideias soltas", if (loose == 1) "1 lugar sem viagem" else "$loose lugares sem viagem", null)
                    }
                }
            }
        }
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
private fun TripChip(title: String, choices: List<Pair<String, String>>, onSelect: (String) -> Unit) {
    var open by remember { mutableStateOf(false) }
    Box {
        Row(Modifier.heightIn(min = 44.dp).clip(RoundedCornerShape(22.dp)).background(arkeInsetColor())
            .clickable { open = true }.padding(horizontal = 14.dp).semantics { contentDescription = "Viagem: $title" },
            verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Icon(painterResource(R.drawable.ic_suitcase), null, Modifier.size(18.dp))
            Text(title, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
            Text("⌄")
        }
        DropdownMenu(expanded = open, onDismissRequest = { open = false }) {
            choices.forEach { (id, text) -> DropdownMenuItem(text = { Text(text) }, onClick = { open = false; onSelect(id) }) }
        }
    }
}

@Composable
private fun PillOption(label: String, selected: Boolean, enabled: Boolean = true, onClick: () -> Unit) {
    Box(Modifier.heightIn(min = 44.dp).widthIn(min = 64.dp).clip(RoundedCornerShape(22.dp))
        .background(if (selected) ArkeColors.brand else arkeInsetColor())
        .clickable(enabled = enabled, onClick = onClick).padding(horizontal = 16.dp),
        contentAlignment = Alignment.Center) {
        Text(label, color = if (selected) androidx.compose.ui.graphics.Color.White else MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.alpha(if (enabled) 1f else 0.4f))
    }
}

@Composable
private fun Stars(rating: Int) {
    Text("★".repeat(rating) + "☆".repeat((5 - rating).coerceAtLeast(0)) + "  $rating de 5",
        color = androidx.compose.ui.graphics.Color(0xFFB26A00),
        modifier = Modifier.semantics { contentDescription = "Nota $rating de 5" })
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TripJournalContent(plan: LocalPinPlan?, ideas: List<PlaceIdea>, reviews: Boolean, detailed: Boolean, preview: Boolean = false) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val prefs = remember(context) { context.getSharedPreferences("arke.trip-journal.v1", 0) }
    var raw by remember(prefs) { mutableStateOf(prefs.getString("records", "[]") ?: "[]") }
    var tripID by remember { mutableStateOf<String?>(null) }
    val activePlan = if (preview) LocalPinPlan(emptyList(), listOf(LocalPinTrip("preview", "Milano"))) else plan
    val trip = activePlan?.trips?.firstOrNull { it.id == tripID } ?: activePlan?.trips?.firstOrNull()
    if (activePlan == null || trip == null) {
        Text("Crie uma viagem marcando um lugar no mapa para começar.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        return
    }
    var showingForm by remember(trip.id, reviews) { mutableStateOf(false) }
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
    val source = if (preview) listOf(org.json.JSONObject().put("trip", "preview")
        .put("text", if (reviews) "Chegamos cedo e encontramos a praça tranquila." else "A primeira manhã na praça do Duomo.")
        .put("rating", if (reviews) 4 else 0)) else records
    val entries = source.filter { it.optString("trip") == trip.id && (it.optInt("rating") > 0) == reviews }
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            TripChip(trip.title, activePlan.trips.map { it.id to it.title }) { tripID = it }
            if (!reviews) Text("Privado", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        if (reviews) {
            if (entries.isEmpty()) Text("Nenhuma avaliação nesta viagem.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            (if (detailed) entries.asReversed() else entries.takeLast(1)).forEach { entry ->
                Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(22.dp)).background(arkeInsetColor()).padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Stars(entry.getInt("rating"))
                    Text(entry.getString("text"), maxLines = if (detailed) Int.MAX_VALUE else 3,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
                }
            }
            Button(onClick = { showingForm = true }, enabled = !preview, shape = CircleShape,
                colors = ButtonDefaults.buttonColors(containerColor = ArkeColors.brand, contentColor = androidx.compose.ui.graphics.Color.White),
                modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp).testTag("reviews.add")) { Text("Avaliar este lugar") }
        } else {
            val shown = if (detailed) entries.asReversed() else entries.takeLast(2)
            val cells: List<org.json.JSONObject?> = shown + listOf<org.json.JSONObject?>(null)
            cells.chunked(2).forEach { rowCells ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    rowCells.forEach { entry ->
                        if (entry != null) {
                            Column(Modifier.weight(1f).clip(RoundedCornerShape(18.dp)).background(arkeInsetColor()).padding(8.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                JournalMemoryCover(ideas, entry.optString("place"), preview, Modifier.fillMaxWidth().height(84.dp))
                                Text(entry.getString("text"), maxLines = 2, style = MaterialTheme.typography.bodySmall,
                                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
                            }
                        } else {
                            Box(Modifier.weight(1f).heightIn(min = 120.dp).clip(RoundedCornerShape(18.dp))
                                .border(1.5.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(18.dp))
                                .clickable(enabled = !preview) { showingForm = true }.testTag("memories.add"),
                                contentAlignment = Alignment.Center) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(painterResource(R.drawable.ic_add), null)
                                    Text("Nova memória", style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }
                    }
                    if (rowCells.size == 1) Spacer(Modifier.weight(1f))
                }
            }
            if (entries.isEmpty()) Text("Nenhuma memória nesta viagem.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
    if (showingForm) {
        ArkeFloatingSheet(onDismiss = { showingForm = false }, tagPrefix = "journal",
            modifier = Modifier.fillMaxHeight(0.9f).imePadding()) {
            Box(Modifier.fillMaxWidth().weight(1f)) {
                Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp).padding(top = 28.dp, bottom = 100.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(if (reviews) "Avaliar este lugar" else "Nova memória", style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold, modifier = Modifier.semantics { heading() })
                    if (reviews) {
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            (1..5).forEach { index ->
                                IconButton(onClick = { rating = index }, modifier = Modifier.size(48.dp)
                                    .semantics { contentDescription = "$index de 5" }) {
                                    Text(if (index <= rating) "★" else "☆", style = MaterialTheme.typography.headlineMedium,
                                        color = androidx.compose.ui.graphics.Color(0xFFB26A00))
                                }
                            }
                        }
                    } else {
                        val covers = ideas.filter { idea ->
                            idea.cover.isNotEmpty() && activePlan.pins.any { it.ideaID == idea.id && it.tripID == trip.id }
                        }
                        FinanceChoice("Capa", covers.firstOrNull { it.id == placeID }?.name ?: "Sem capa",
                            listOf("" to "Sem capa") + covers.map { it.id to it.name }) { placeID = it }
                    }
                    OutlinedTextField(draft, { draft = it }, minLines = 4, modifier = Modifier.fillMaxWidth(),
                        label = { Text(if (reviews) "Escreva sua avaliação" else "Escreva sua memória") })
                    if (failed) Text("Não foi possível salvar.", color = MaterialTheme.colorScheme.error)
                    TextButton(onClick = { showingForm = false }, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) { Text("Cancelar") }
                }
                Button(onClick = {
                    val entry = org.json.JSONObject().put("trip", trip.id).put("text", draft.trim())
                    if (reviews) entry.put("rating", rating) else entry.put("place", placeID)
                    val updated = org.json.JSONArray(records + entry).toString()
                    if (prefs.edit().putString("records", updated).commit()) {
                        raw = updated; draft = ""; placeID = ""; failed = false; showingForm = false
                    } else failed = true
                }, enabled = !preview && draft.isNotBlank() && draft.length <= 1000, shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(containerColor = ArkeColors.brand, contentColor = androidx.compose.ui.graphics.Color.White),
                    modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth().padding(horizontal = 20.dp).padding(bottom = 14.dp).heightIn(min = 56.dp)) {
                    Text(if (reviews) "Salvar avaliação" else "Salvar memória")
                }
            }
        }
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
    Box(modifier.clip(RoundedCornerShape(10.dp)).background(arkeSheetColor()), contentAlignment = Alignment.Center) {
        if (image != null) androidx.compose.foundation.Image(image.asImageBitmap(), null, Modifier.fillMaxSize(),
            contentScale = androidx.compose.ui.layout.ContentScale.Crop)
        else Icon(painterResource(R.drawable.ic_photo), null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LocalFinanceContent(trips: List<LocalPinTrip>, savings: Boolean, detailed: Boolean) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val financeFocus = LocalFocusManager.current
    val prefs = remember(context) { context.getSharedPreferences("arke.local-finance.v1", 0) }
    var raw by remember(prefs) { mutableStateOf(prefs.getString("records", "[]") ?: "[]") }
    var tripID by remember { mutableStateOf<String?>(null) }
    val trip = trips.firstOrNull { it.id == tripID } ?: trips.firstOrNull()
    if (trip == null) {
        Text("Crie uma viagem marcando um lugar no mapa para começar.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        return
    }
    var showingForm by remember(trip.id, savings) { mutableStateOf(false) }
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
    /** Valor com símbolo e casas da moeda (sem "BRL 95"). */
    fun money(value: Long, code: String): String = java.text.NumberFormat.getCurrencyInstance().apply {
        this.currency = java.util.Currency.getInstance(code)
        minimumFractionDigits = digits(code)
        maximumFractionDigits = digits(code)
    }.format(java.math.BigDecimal.valueOf(value, digits(code)))
    fun clearDraft() { editing = null; kind = ""; name = ""; amount = ""; error = null }
    fun kindLabel(k: String) = when (k) { "planned" -> "Prevista"; "incurred" -> "Realizada"; "goal" -> "Meta"; else -> "Valor reservado" }

    val acceptedKinds = if (savings) setOf("goal", "saving") else setOf("planned", "incurred")
    val entries = records.filter { it.optString("trip") == trip.id && it.optString("kind") in acceptedKinds }
    val goal = entries.firstOrNull { it.getString("kind") == "goal" }
    val goalCurrency = goal?.getString("currency")
    val contributions = entries.filter { it.getString("kind") == "saving" }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            TripChip(trip.title, trips.map { it.id to it.title }) { tripID = it }
            if (savings) Text("Só você vê", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        if (savings && goal != null && goalCurrency != null) {
            val total = contributions.filter { it.getString("currency") == goalCurrency }.sumOf { it.getLong("amount") }
            Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(22.dp)).background(arkeInsetColor()).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Reservado", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("${money(total, goalCurrency)} de ${money(goal.getLong("amount"), goalCurrency)}", style = MaterialTheme.typography.titleMedium)
                LinearProgressIndicator(progress = { (total.toDouble() / goal.getLong("amount")).toFloat().coerceIn(0f, 1f) },
                    color = ArkeColors.brand, modifier = Modifier.fillMaxWidth())
                Text("Falta ${money((goal.getLong("amount") - total).coerceAtLeast(0), goalCurrency)}", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        if (!savings) {
            entries.groupBy { it.getString("currency") }.toSortedMap().forEach { (code, subtotal) ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("Previsto" to "planned", "Realizado" to "incurred").forEach { (title, k) ->
                        Column(Modifier.weight(1f).clip(RoundedCornerShape(20.dp)).background(arkeInsetColor()).padding(14.dp)) {
                            Text(title, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(money(subtotal.filter { it.getString("kind") == k }.sumOf { it.getLong("amount") }, code),
                                style = MaterialTheme.typography.titleMedium, maxLines = 1)
                        }
                    }
                }
            }
        }
        if (entries.isEmpty()) {
            Text(if (savings) "Defina uma meta para começar. É só um registro: o Arke não guarda dinheiro." else "Nenhum gasto nesta viagem ainda.",
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        } else {
            Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(22.dp)).background(arkeInsetColor())) {
                (if (detailed) entries else entries.takeLast(3)).forEachIndexed { i, entry ->
                    if (i > 0) HorizontalDivider(Modifier.padding(start = 56.dp))
                    val code = entry.getString("currency")
                    val label = "${entry.getString("name")}, ${kindLabel(entry.getString("kind"))}, ${money(entry.getLong("amount"), code)}"
                    Row(Modifier.fillMaxWidth().heightIn(min = 56.dp).clickable {
                        editing = entry.getString("id"); kind = entry.getString("kind"); name = entry.getString("name")
                        currency = code; amount = display(entry.getLong("amount"), code); error = null; showingForm = true
                    }.padding(horizontal = 16.dp).semantics { contentDescription = label },
                        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Icon(painterResource(if (entry.getString("kind") == "incurred") R.drawable.ic_check else if (entry.getString("kind") == "planned") R.drawable.ic_clock else R.drawable.ic_target),
                            null, Modifier.size(22.dp), tint = if (isSystemInDarkTheme()) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.primary)
                        Column(Modifier.weight(1f)) {
                            Text(entry.getString("name"), maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
                            Text(kindLabel(entry.getString("kind")), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Text(money(entry.getLong("amount"), code), fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
        Button(onClick = {
            clearDraft()
            if (savings) { kind = if (goal == null) "goal" else "saving"; if (goalCurrency != null) currency = goalCurrency } else kind = "planned"
            showingForm = true
        }, shape = CircleShape,
            colors = ButtonDefaults.buttonColors(containerColor = ArkeColors.brand, contentColor = androidx.compose.ui.graphics.Color.White),
            modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp).testTag(if (savings) "savings.add" else "expenses.add")) {
            Text(if (savings) (if (goal == null) "Definir meta" else "Registrar valor") else "Adicionar gasto")
        }
        if (savings) Text("Registro informativo: o Arke não processa pagamentos nem guarda dinheiro.",
            style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }

    if (showingForm) {
        val code = if (savings && (kind == "saving" || contributions.isNotEmpty())) goalCurrency ?: currency else currency
        val types = if (savings) {
            if (goal == null) listOf("goal" to "Meta") else listOf("goal" to "Meta", "saving" to "Valor reservado")
        } else listOf("planned" to "Prevista", "incurred" to "Realizada")
        ArkeFloatingSheet(onDismiss = { showingForm = false; clearDraft() }, tagPrefix = "finance",
            modifier = Modifier.fillMaxHeight(0.9f).imePadding()) {
            Box(Modifier.fillMaxWidth().weight(1f)) {
                Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp).padding(top = 28.dp, bottom = 100.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(if (editing == null) (if (savings) (if (goal == null) "Definir meta" else "Registrar valor") else "Adicionar gasto") else "Editar registro",
                        style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, modifier = Modifier.semantics { heading() })
                    Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).background(arkeInsetColor()).padding(16.dp)) {
                        Text("Valor", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(java.util.Currency.getInstance(code).symbol, style = MaterialTheme.typography.headlineSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                            androidx.compose.foundation.text.BasicTextField(amount, { amount = it }, singleLine = true,
                                textStyle = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface),
                                cursorBrush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.onSurface),
                                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal),
                                modifier = Modifier.weight(1f).testTag("finance.amount"))
                        }
                    }
                    Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("BRL", "USD", "EUR", "GBP", "JPY", "KWD").forEach { c ->
                            val locked = savings && (kind == "saving" || contributions.isNotEmpty())
                            PillOption(c, selected = code == c, enabled = !locked) { currency = c }
                        }
                    }
                    if (!savings) OutlinedTextField(name, { name = it }, label = { Text("Descrição") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        types.forEach { (id, title) -> PillOption(title, selected = kind == id, enabled = !savings || editing == null) { kind = id } }
                    }
                    error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                    Text("Registro informativo: o Arke não processa pagamentos.", style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    TextButton(onClick = { showingForm = false; clearDraft() }, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) { Text("Cancelar") }
                }
                Button(onClick = {
                    val separator = java.text.DecimalFormatSymbols.getInstance().decimalSeparator
                    val pieces = amount.trim().split(separator)
                    val value = pieces.joinToString(".").toBigDecimalOrNull()
                    val valid = pieces.size <= 2 && pieces.all { it.isNotEmpty() && it.all { digit -> digit in '0'..'9' } } &&
                        (pieces.getOrNull(1)?.length ?: 0) <= digits(code) && value != null &&
                        value > java.math.BigDecimal.ZERO && value <= java.math.BigDecimal("1000000000")
                    if (!valid || kind.isEmpty() || (!savings && name.isBlank()) || (kind == "saving" && goal == null)) {
                        error = "Informe uma descrição (nos gastos) e um valor válido para a moeda."
                        return@Button
                    }
                    val existing = editing ?: if (kind == "goal") goal?.getString("id") else null
                    if (existing == null && records.size >= 10_000) {
                        error = "Limite de registros atingido."
                        return@Button
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
                        showingForm = false
                        clearDraft()
                    } else {
                        error = "Não foi possível salvar os registros."
                    }
                }, shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(containerColor = ArkeColors.brand, contentColor = androidx.compose.ui.graphics.Color.White),
                    modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth().padding(horizontal = 20.dp).padding(bottom = 14.dp).heightIn(min = 56.dp).testTag("finance.save")) {
                    Text(if (editing == null) "Salvar registro" else "Salvar alterações")
                }
            }
        }
    }
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
