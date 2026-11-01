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
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
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
            Card(
                modifier = Modifier.fillMaxWidth().weight(mapWeight),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            ) {
                TravelMap(Modifier.fillMaxSize())
            }
            Card(
                modifier = Modifier.fillMaxWidth().weight(1f - mapWeight),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            ) {
                Column(Modifier.padding(16.dp).verticalScroll(rememberScrollState())) {
                    ContextMenu(state.context) { state = state.selectContext(it) }
                    DetentMenu(state.detent) { state = state.setDetent(it) }
                    HorizontalDivider(Modifier.padding(vertical = 12.dp))
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

@Composable
private fun ContextMenu(selected: TravelContext, onSelect: (TravelContext) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val description = stringResource(R.string.change_context, stringResource(selected.title()))
    Box {
        TextButton(
            onClick = { expanded = true },
            colors = ButtonDefaults.textButtonColors(
                contentColor = if (isSystemInDarkTheme()) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.primary,
            ),
            modifier = Modifier.heightIn(min = 48.dp).semantics { contentDescription = description },
        ) { Text(stringResource(selected.title())) }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            TravelContext.entries.forEach { context ->
                DropdownMenuItem(
                    text = { Text(stringResource(context.title())) },
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
            modifier = Modifier.heightIn(min = 48.dp).semantics { contentDescription = description },
        ) { Text(stringResource(selected.title())) }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            PanelDetent.entries.forEach { detent ->
                DropdownMenuItem(
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
