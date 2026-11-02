package dev.arke.app

import androidx.annotation.StringRes
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.foundation.background
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
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.selected
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
fun ArkeHome(places: PlaceIdeasViewModel, captureDetails: Boolean = false) {
    val placeState by places.state.collectAsState()
    var savedNotice by rememberSaveable { mutableStateOf(false) }
    var pendingSaveId by rememberSaveable { mutableStateOf<String?>(null) }
    var state by rememberSaveable(stateSaver = HomeStateSaver) { mutableStateOf(HomeState()) }
    var showingSavedDetails by remember { mutableStateOf(false) }
    var showingAddPlace by rememberSaveable { mutableStateOf(false) }
    var showingDetailPreview by rememberSaveable { mutableStateOf(captureDetails) }
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
    val modalVisible = showingAddPlace || showingDetailPreview || showingSavedDetails
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
                val mapHeight = with(density) { dragHeight?.toDp() } ?: restingHeight
                val dragModifier = Modifier.draggable(
                    orientation = Orientation.Vertical,
                    state = rememberDraggableState { delta ->
                        val current = dragHeight ?: with(density) { restingHeight.toPx() }
                        dragHeight = (current + delta).coerceIn(minMapPx, maxMapPx)
                    },
                    onDragStarted = { dragHeight = with(density) { restingHeight.toPx() } },
                    onDragStopped = {
                        val target = dragHeight
                        if (target != null) {
                            val closest = PanelDetent.entries.minBy { abs(with(density) { heightFor(it).toPx() } - target) }
                            state = state.setDetent(closest)
                        }
                        dragHeight = null
                    },
                )
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Card(
                        modifier = Modifier.fillMaxWidth().height(mapHeight),
                        shape = RoundedCornerShape(20.dp),
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
                            Column(Modifier.weight(1f).testTag("home.contentBody").verticalScroll(rememberScrollState())) {
                                if (savedNotice) Text(stringResource(R.string.saved_place_notice), modifier = Modifier.testTag("place.savedNotice"), style = MaterialTheme.typography.bodySmall)
                                if (placeState.error != null && !showingAddPlace) {
                                    Text(placeState.error.orEmpty(), color = MaterialTheme.colorScheme.error)
                                    TextButton(onClick = places::load) { Text(stringResource(R.string.retry)) }
                                }
                                if (placeState.ready) {
                                    if (state.context == TravelContext.PLANNING) PlaceIdeasContent(placeState, onDetailsVisibilityChanged = { showingSavedDetails = it }) { id -> savedNotice = false; places.remove(id) }
                                    else {
                                        Text(stringResource(state.context.title()), style = MaterialTheme.typography.titleLarge)
                                        Text(stringResource(R.string.context_pending))
                                        Text(stringResource(R.string.places_in_planning))
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
}

@Composable
private fun CardHeader(context: TravelContext, onAdd: () -> Unit, addEnabled: Boolean, onSelect: (TravelContext) -> Unit) {
    val actionColor = if (isSystemInDarkTheme()) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.primary
    Row(Modifier.fillMaxWidth(), verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
        IconButton(onClick = onAdd, enabled = addEnabled, modifier = Modifier.size(48.dp).testTag("home.addPlace")) {
            Icon(painterResource(R.drawable.ic_add), contentDescription = stringResource(R.string.add_place), tint = actionColor)
        }
        Text(
            stringResource(context.title()),
            modifier = Modifier.weight(1f).padding(horizontal = 8.dp).testTag("home.contextTitle").semantics { heading() },
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
        )
        ContextMenu(context, onSelect)
    }
}

@Composable
private fun ContextMenu(active: TravelContext, onSelect: (TravelContext) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val description = stringResource(R.string.change_context, stringResource(active.title()))
    val actionColor = if (isSystemInDarkTheme()) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.primary
    Box {
        IconButton(
            onClick = { expanded = true },
            modifier = Modifier.size(48.dp).testTag("home.contextMenu").semantics { contentDescription = description },
        ) { Icon(painterResource(R.drawable.ic_clock), contentDescription = null, tint = actionColor) }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            TravelContext.entries.forEach { context ->
                DropdownMenuItem(
                    modifier = Modifier.testTag("context.${context.name}").semantics { selected = context == active },
                    text = { Text(stringResource(context.title())) },
                    trailingIcon = { if (context == active) Text("✓") },
                    onClick = { onSelect(context); expanded = false },
                )
            }
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
