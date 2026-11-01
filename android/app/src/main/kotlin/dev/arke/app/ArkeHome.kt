package dev.arke.app

import androidx.annotation.StringRes
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
fun ArkeHome() {
    var state by rememberSaveable(stateSaver = HomeStateSaver) { mutableStateOf(HomeState()) }
    var showingAddPlace by rememberSaveable { mutableStateOf(false) }
    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            Modifier.safeDrawingPadding().imePadding().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(stringResource(R.string.app_name), style = MaterialTheme.typography.titleLarge)
            Text(stringResource(R.string.tagline), style = MaterialTheme.typography.headlineMedium)
            val mapWeight = when (state.detent) {
                PanelDetent.EXPLORE -> 0.65f
                PanelDetent.BALANCED -> 0.45f
                PanelDetent.DETAILS -> 0.25f
            }
            BoxWithConstraints(Modifier.fillMaxWidth().weight(1f)) {
                val minContentHeight = (160 * LocalDensity.current.fontScale).dp
                val mapHeight = maxOf(64.dp, minOf(maxHeight * mapWeight, maxHeight - minContentHeight - 64.dp))
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Card(
                        modifier = Modifier.fillMaxWidth().height(mapHeight),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                    ) { TravelMap(Modifier.fillMaxSize()) }

                    Box(Modifier.fillMaxWidth(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                        DetentMenu(state.detent) { state = state.setDetent(it) }
                    }

                    Card(
                        modifier = Modifier.fillMaxWidth().weight(1f),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            CardHeader(state.context, { showingAddPlace = true }) { state = state.selectContext(it) }
                            HorizontalDivider(Modifier.padding(vertical = 12.dp))
                            Column(Modifier.weight(1f).verticalScroll(rememberScrollState())) {
                                Text(
                                    stringResource(if (state.context == TravelContext.PLANNING) R.string.empty_title else state.context.title()),
                                    style = MaterialTheme.typography.titleLarge,
                                )
                                Spacer(Modifier.height(12.dp))
                                Text(stringResource(if (state.context == TravelContext.PLANNING) R.string.empty_detail else R.string.context_pending))
                                Spacer(Modifier.height(24.dp))
                                Text(stringResource(R.string.development_notice), style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            }
        }
    }
    if (showingAddPlace) {
        AlertDialog(
            onDismissRequest = { showingAddPlace = false },
            title = { Text(stringResource(R.string.add_place)) },
            text = { Text(stringResource(R.string.add_place_pending)) },
            confirmButton = { TextButton(onClick = { showingAddPlace = false }) { Text(stringResource(R.string.understood)) } },
        )
    }
}

@Composable
private fun CardHeader(context: TravelContext, onAdd: () -> Unit, onSelect: (TravelContext) -> Unit) {
    val actionColor = if (isSystemInDarkTheme()) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.primary
    Row(Modifier.fillMaxWidth(), verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
        IconButton(onClick = onAdd, modifier = Modifier.size(48.dp).testTag("home.addPlace")) {
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
private fun DetentMenu(selected: PanelDetent, onSelect: (PanelDetent) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val description = stringResource(R.string.change_panel, stringResource(selected.title()))
    Box {
        TextButton(
            onClick = { expanded = true },
            colors = ButtonDefaults.textButtonColors(
                contentColor = if (isSystemInDarkTheme()) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.primary,
            ),
            modifier = Modifier.heightIn(min = 48.dp).testTag("home.panelMenu").semantics { contentDescription = description },
        ) { Text(stringResource(selected.title()) + " ↕", style = MaterialTheme.typography.labelMedium) }
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
