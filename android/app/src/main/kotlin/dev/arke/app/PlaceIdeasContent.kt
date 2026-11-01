package dev.arke.app

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.text.style.TextAlign
import dev.arke.core.PlaceDraft
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import dev.arke.core.PlaceIdea

@Composable
fun PlaceIdeasContent(state: PlaceIdeasUiState, onDelete: (String) -> Unit) {
    var deleting by remember { mutableStateOf<PlaceIdea?>(null) }
    var selectedID by rememberSaveable { mutableStateOf<String?>(null) }
    if (state.ideas.isEmpty()) {
        Text(stringResource(R.string.empty_title), style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(12.dp))
        Text(stringResource(R.string.empty_detail))
    } else {
        Text(stringResource(R.string.local_ideas), style = MaterialTheme.typography.titleLarge)
        state.ideas.forEach { idea ->
            key(idea.id) {
                Spacer(Modifier.height(16.dp))
                Text(idea.name, style = MaterialTheme.typography.titleMedium)
                Text(idea.address, color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (idea.note.isNotEmpty()) Text(idea.note)
                TextButton(onClick = { selectedID = idea.id }, modifier = Modifier.heightIn(min = 48.dp)
                    .testTag("place.open.${idea.id}").semantics { contentDescription = "Ver detalhes de ${idea.name}" }) {
                    Text(stringResource(R.string.view_place_details))
                }
                val deleteLabel = stringResource(R.string.delete_place) + ": " + idea.name
                TextButton(
                    onClick = { deleting = idea }, enabled = !state.busy,
                    modifier = Modifier.testTag("place.delete.${idea.id}").semantics { contentDescription = deleteLabel },
                ) { Text(stringResource(R.string.delete_place), color = MaterialTheme.colorScheme.error) }
                HorizontalDivider()
            }
        }
    }
    Spacer(Modifier.height(24.dp))
    Text(stringResource(R.string.development_notice), style = MaterialTheme.typography.bodySmall)
    selectedID?.let { id -> PlaceDetails(state.ideas, id, onClose = { selectedID = null }) }
    if (deleting != null) {
        AlertDialog(
            onDismissRequest = { deleting = null },
            title = { Text(stringResource(R.string.delete_place_title)) },
            text = { Text(stringResource(R.string.delete_place_detail)) },
            confirmButton = { TextButton(onClick = { deleting?.id?.let(onDelete); deleting = null }, modifier = Modifier.testTag("place.confirmDelete")) { Text(stringResource(R.string.delete_confirm)) } },
            dismissButton = { TextButton(onClick = { deleting = null }) { Text(stringResource(R.string.cancel)) } },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaceDetails(ideas: List<PlaceIdea>, initialID: String, onClose: () -> Unit) {
    var selectedID by rememberSaveable(initialID) { mutableStateOf(initialID) }
    val index = ideas.indexOfFirst { it.id == selectedID }
    val sheetColor = MaterialTheme.colorScheme.surfaceContainerLow
    val actionColor = if (isSystemInDarkTheme()) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.primary
    val buttonColors = ButtonDefaults.textButtonColors(contentColor = actionColor)
    ModalBottomSheet(
        onDismissRequest = onClose, sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = sheetColor, tonalElevation = 0.dp,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        dragHandle = {
            Box(Modifier.fillMaxWidth().height(24.dp), contentAlignment = Alignment.Center) {
                Box(Modifier.size(36.dp, 4.dp).background(MaterialTheme.colorScheme.onSurfaceVariant, RoundedCornerShape(2.dp)))
            }
        },
    ) {
        Column(Modifier.fillMaxWidth().fillMaxHeight(0.9f).padding(horizontal = 20.dp)) {
            Row(Modifier.fillMaxWidth().heightIn(min = 48.dp), verticalAlignment = Alignment.CenterVertically) {
                TextButton(onClick = onClose, colors = buttonColors, contentPadding = PaddingValues(0.dp),
                    modifier = Modifier.width(88.dp).heightIn(min = 48.dp).testTag("detail.close")) {
                    Text(stringResource(R.string.close_details), Modifier.fillMaxWidth(), textAlign = TextAlign.Start)
                }
                Text(stringResource(R.string.place_details), Modifier.weight(1f).testTag("detail.title").semantics { heading() },
                    style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center)
                Spacer(Modifier.width(88.dp))
            }
            key(selectedID) {
                Column(Modifier.fillMaxWidth().weight(1f).drawWithContent {
                    drawContent()
                    drawRect(Brush.verticalGradient(listOf(Color.Transparent, sheetColor),
                        startY = (size.height - 32.dp.toPx()).coerceAtLeast(0f), endY = size.height))
                }.verticalScroll(rememberScrollState()).testTag("detail.body").padding(top = 24.dp, bottom = 48.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    if (index >= 0) {
                        val idea = ideas[index]
                        DetailSection(stringResource(R.string.place_name), idea.name, "detail.name", true)
                        DetailSection(stringResource(R.string.place_address), idea.address, "detail.address")
                        DetailSection(stringResource(R.string.place_note), idea.note.ifEmpty { stringResource(R.string.no_note) }, "detail.note")
                    } else Text(stringResource(R.string.place_unavailable))
                }
            }
            Column(Modifier.fillMaxWidth().padding(bottom = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                if (index >= 0) Text(stringResource(R.string.place_position, index + 1, ideas.size),
                    Modifier.fillMaxWidth().testTag("detail.position").semantics { liveRegion = LiveRegionMode.Polite },
                    style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Center)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    TextButton(onClick = { if (index > 0) selectedID = ideas[index - 1].id }, enabled = index > 0, colors = buttonColors,
                        modifier = Modifier.weight(1f).heightIn(min = 48.dp).testTag("detail.previous")) { Text(stringResource(R.string.previous_place)) }
                    TextButton(onClick = { if (index >= 0 && index + 1 < ideas.size) selectedID = ideas[index + 1].id },
                        enabled = index >= 0 && index + 1 < ideas.size, colors = buttonColors,
                        modifier = Modifier.weight(1f).heightIn(min = 48.dp).testTag("detail.next")) { Text(stringResource(R.string.next_place)) }
                }
                Text(stringResource(R.string.place_detail_notice), style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
private fun DetailSection(label: String, value: String, tag: String, title: Boolean = false) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(label, style = MaterialTheme.typography.labelMedium)
        Text(value, style = if (title) MaterialTheme.typography.headlineSmall else MaterialTheme.typography.bodyLarge,
            modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surfaceContainerHighest, RoundedCornerShape(12.dp))
                .padding(16.dp).testTag(tag))
    }
}

internal fun detailCaptureIdeas(): List<PlaceIdea> {
    check(BuildConfig.DEBUG)
    return listOf(
        PlaceIdea.from(PlaceDraft(id = "capture-garden", name = "Jardim das descobertas", address = "Entrada principal, perto da praça central", note = "Reservar uma manhã para caminhar, conhecer os jardins e fazer uma pausa com calma. Esta é uma ideia de demonstração, sem ponto marcado no mapa.")),
        PlaceIdea.from(PlaceDraft(id = "capture-museum", name = "Museu dos caminhos", address = "Avenida das ideias, 100")),
    )
}
