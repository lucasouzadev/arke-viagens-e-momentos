package dev.arke.app

import androidx.annotation.StringRes
import androidx.compose.animation.core.Animatable
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
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
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
import dev.arke.core.HomeState
import dev.arke.core.PanelDetent
import dev.arke.core.TravelContext

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
            BoxWithConstraints(Modifier.fillMaxWidth().weight(1f)) {
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
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Card(
                        modifier = Modifier.fillMaxWidth().height(mapHeight)
                            .border(1.dp, androidx.compose.ui.graphics.Color.White.copy(alpha = 0.42f), RoundedCornerShape(30.dp)),
                        shape = RoundedCornerShape(30.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                    ) { TravelMap(Modifier.fillMaxSize()) }

                    Box(Modifier.fillMaxWidth(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                        DetentMenu(state.detent, dragModifier) { state = state.setDetent(it) }
                    }

                    Card(
                        modifier = Modifier.fillMaxWidth().weight(1f),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                    ) {
                        Column(Modifier.padding(20.dp)) {
                            CardHeader(state.context, { showingAddPlace = true }, placeState.ready && !placeState.busy) { state = state.selectContext(it) }
                            HorizontalDivider(Modifier.padding(vertical = 12.dp))
                            Column(Modifier.weight(1f).arkeScrollEdges().testTag("home.contentBody").verticalScroll(rememberScrollState())) {
                                if (savedNotice) Text(stringResource(R.string.saved_place_notice), modifier = Modifier.testTag("place.savedNotice"), style = MaterialTheme.typography.bodySmall)
                                if (placeState.error != null && !showingAddPlace) {
                                    Text(placeState.error.orEmpty(), color = MaterialTheme.colorScheme.error)
                                    TextButton(onClick = places::load) { Text(stringResource(R.string.retry)) }
                                }
                                if (placeState.ready) {
                                    if (state.context == TravelContext.PLANNING) PlaceIdeasContent(placeState,
                                        onUpdate = places::update, onEditCancel = places::load,
                                        onDetailsVisibilityChanged = { showingSavedDetails = it }) { id -> savedNotice = false; places.remove(id) }
                                    else {
                                        Text("Em breve.")
                                        TextButton(onClick = { state = state.selectContext(TravelContext.PLANNING) }) { Text(stringResource(R.string.view_places)) }
                                    }
                                } else if (placeState.busy) Text(stringResource(R.string.loading_places))

                            }
                        }
                    }
                }
            }
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
