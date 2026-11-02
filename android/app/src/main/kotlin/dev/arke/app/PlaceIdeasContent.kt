package dev.arke.app

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.text.style.TextAlign
import dev.arke.core.PlaceDraft
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.Saver
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import dev.arke.core.PlaceIdea

@Composable
fun PlaceIdeasContent(state: PlaceIdeasUiState, onUpdate: (PlaceDraft, PlaceIdea, String) -> Unit, onEditCancel: () -> Unit,
    onDetailsVisibilityChanged: (Boolean) -> Unit = {}, onDelete: (String) -> Unit) {
    var deleting by remember { mutableStateOf<PlaceIdea?>(null) }
    var selectedID by rememberSaveable { mutableStateOf<String?>(null) }
    var editing by rememberSaveable(stateSaver = IdeaSaver) { mutableStateOf<PlaceIdea?>(null) }
    var pendingUpdate by rememberSaveable(stateSaver = PendingDraftSaver) { mutableStateOf<PlaceDraft?>(null) }
    var pendingRequest by rememberSaveable { mutableStateOf<String?>(null) }
    LaunchedEffect(selectedID, editing) { onDetailsVisibilityChanged(selectedID != null || editing != null) }
    LaunchedEffect(state, pendingUpdate, pendingRequest) {
        val pending = pendingUpdate
        if (pending != null && pendingRequest != null && state.completedUpdate == pendingRequest &&
            state.ready && !state.busy && state.error == null && state.ideas.any { it == PlaceIdea.from(pending) }) {
            editing = null
            pendingUpdate = null
            pendingRequest = null
        }
    }
    DisposableEffect(Unit) { onDispose { onDetailsVisibilityChanged(false) } }
    if (state.ideas.isEmpty()) {
        Text("Guarde seu próximo lugar com +", style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp), textAlign = TextAlign.Center)
    } else {
        state.ideas.forEach { idea ->
            key(idea.id) {
                Spacer(Modifier.height(16.dp))
                Column(Modifier.fillMaxWidth().background(arkeInsetColor(), RoundedCornerShape(24.dp)).padding(16.dp)) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text(idea.name, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                    IconButton(onClick = { pendingUpdate = null; pendingRequest = null; editing = idea }, enabled = state.ready && !state.busy,
                        modifier = Modifier.size(48.dp).testTag("place.update.${idea.id}")) {
                        Icon(painterResource(R.drawable.ic_edit), stringResource(R.string.edit_place_label, idea.name))
                    }
                }
                Text(idea.address, color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (idea.note.isNotEmpty()) Text(idea.note)
                val openLabel = stringResource(R.string.view_place_details_label, idea.name)
                TextButton(onClick = { selectedID = idea.id },
                    colors = ButtonDefaults.textButtonColors(contentColor = if (isSystemInDarkTheme()) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.primary),
                    contentPadding = PaddingValues(0.dp), modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)
                    .testTag("place.open.${idea.id}").semantics { contentDescription = openLabel }) {
                    Text("Abrir ↗", Modifier.fillMaxWidth(), textAlign = TextAlign.Start)
                }
                val deleteLabel = stringResource(R.string.delete_place) + ": " + idea.name
                TextButton(
                    onClick = { deleting = idea }, enabled = !state.busy,
                    modifier = Modifier.testTag("place.delete.${idea.id}").semantics { contentDescription = deleteLabel },
                ) { Icon(painterResource(R.drawable.ic_delete), null, tint = MaterialTheme.colorScheme.error) }
                }
            }
        }
    }
    Spacer(Modifier.height(24.dp))
    selectedID?.let { id -> PlaceDetails(state.ideas, id, onClose = { selectedID = null }) }
    editing?.let { original ->
        PlaceEditor(initialIdea = original, saving = state.busy, saveError = if (pendingUpdate != null) state.error else null,
            onSave = { draft ->
                val requestID = java.util.UUID.randomUUID().toString()
                pendingUpdate = draft; pendingRequest = requestID
                onUpdate(draft, original, requestID)
            },
            onCancel = { editing = null; pendingUpdate = null; pendingRequest = null; onEditCancel() })
    }
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

private val IdeaSaver = Saver<PlaceIdea?, List<String>>(
    save = { idea -> idea?.let { listOf(it.id, it.name, it.address, it.note) } },
    restore = { PlaceIdea(it[0], it[1], it[2], it[3]) },
)

private val PendingDraftSaver = Saver<PlaceDraft?, List<String>>(
    save = { draft -> draft?.let { listOf(it.id, it.name, it.address, it.note) } },
    restore = { PlaceDraft(it[0], it[1], it[2], it[3]) },
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaceDetails(ideas: List<PlaceIdea>, initialID: String, onClose: () -> Unit) {
    var selectedID by rememberSaveable(initialID) { mutableStateOf(initialID) }
    var showingInfo by remember { mutableStateOf(false) }
    val index = ideas.indexOfFirst { it.id == selectedID }
    val sheetColor = arkeSheetColor()
    val insetColor = arkeInsetColor()
    val ink = MaterialTheme.colorScheme.onSurface
    val maximumHeight = with(LocalDensity.current) { (LocalWindowInfo.current.containerSize.height * 0.9f).toDp() }
    ArkeFloatingSheet(onDismiss = onClose, tagPrefix = "detail", modifier = Modifier.heightIn(max = maximumHeight)) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp).padding(top = 16.dp)) {
            Row(Modifier.fillMaxWidth().heightIn(min = 48.dp), verticalAlignment = Alignment.CenterVertically) {
                Spacer(Modifier.width(48.dp))
                Text(stringResource(R.string.detail_header), Modifier.weight(1f).testTag("detail.title").semantics { heading() },
                    style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center)
                Box {
                    IconButton(onClick = { showingInfo = true }, modifier = Modifier.size(48.dp).background(insetColor, CircleShape).testTag("detail.info")) {
                        Icon(painterResource(R.drawable.ic_info), stringResource(R.string.detail_info_label), tint = ink)
                    }
                    DropdownMenu(expanded = showingInfo, onDismissRequest = { showingInfo = false }) {
                        Column(Modifier.width(280.dp).padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text(stringResource(R.string.detail_info_title), style = MaterialTheme.typography.titleMedium)
                            Text(stringResource(R.string.place_detail_notice), style = MaterialTheme.typography.bodyLarge, modifier = Modifier.testTag("detail.infoText"))
                            TextButton(onClick = { showingInfo = false }, colors = ButtonDefaults.textButtonColors(contentColor = ink),
                                modifier = Modifier.heightIn(min = 48.dp).testTag("detail.infoDismiss")) { Text(stringResource(R.string.detail_info_dismiss)) }
                        }
                    }
                }
            }
            key(selectedID) {
                Column(Modifier.fillMaxWidth().weight(1f, fill = false).drawWithContent {
                    drawContent()
                    drawRect(Brush.verticalGradient(listOf(sheetColor, Color.Transparent), 0f, 12.dp.toPx()))
                    drawRect(Brush.verticalGradient(listOf(Color.Transparent, sheetColor),
                        startY = (size.height - 32.dp.toPx()).coerceAtLeast(0f), endY = size.height))
                }.verticalScroll(rememberScrollState()).testTag("detail.body").padding(top = 24.dp, bottom = 48.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)) {
                    if (index >= 0) {
                        val idea = ideas[index]
                        val positionLabel = stringResource(R.string.place_position, index + 1, ideas.size)
                        Row(Modifier.padding(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.Top) {
                            Icon(painterResource(R.drawable.ic_bookmark), null, modifier = Modifier.size(24.dp, 28.dp).testTag("detail.nameIcon"))
                            Text(idea.name, style = MaterialTheme.typography.titleLarge,
                                modifier = Modifier.weight(1f).testTag("detail.name").semantics {
                                    heading(); liveRegion = LiveRegionMode.Polite; stateDescription = positionLabel
                                })
                        }
                        Column(Modifier.fillMaxWidth().background(insetColor, RoundedCornerShape(24.dp)).padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(20.dp)) {
                            DetailContentRow(idea.address, R.drawable.ic_pin, stringResource(R.string.place_address), "detail.address")
                            if (idea.note.isNotEmpty()) DetailContentRow(idea.note, R.drawable.ic_note, stringResource(R.string.place_note), "detail.note")
                        }
                    } else Text(stringResource(R.string.place_unavailable))
                }
            }
            Row(Modifier.fillMaxWidth().padding(top = 16.dp, bottom = 16.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                if (index > 0) {
                    IconButton(onClick = { selectedID = ideas[index - 1].id },
                        modifier = Modifier.size(48.dp).background(insetColor, CircleShape).testTag("detail.previous")) {
                        Icon(painterResource(R.drawable.ic_back), stringResource(R.string.previous_place), tint = ink)
                    }
                }
                val isLast = index < 0 || index == ideas.lastIndex
                Button(onClick = { if (isLast) onClose() else selectedID = ideas[index + 1].id }, shape = CircleShape,
                    modifier = Modifier.weight(1f).heightIn(min = 48.dp).testTag(if (isLast) "detail.done" else "detail.next")) {
                    Text(stringResource(if (isLast) R.string.detail_done else R.string.next_place))
                }
            }
        }
    }
}

@Composable
private fun DetailContentRow(value: String, icon: Int, label: String, tag: String) {
    Row(Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.Top) {
        Icon(painterResource(icon), contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f), modifier = Modifier.size(24.dp))
        Text(value, style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f).testTag(tag).semantics { contentDescription = "$label: $value" })
    }
}

internal fun detailCaptureIdeas(): List<PlaceIdea> {
    check(BuildConfig.DEBUG)
    return listOf(
        PlaceIdea.from(PlaceDraft(id = "capture-garden", name = "Jardim das descobertas", address = "Entrada principal, perto da praça central", note = "Chegar pela manhã, caminhar pelos jardins e reservar um tempo para um café.")),
        PlaceIdea.from(PlaceDraft(id = "capture-museum", name = "Museu dos caminhos", address = "Avenida das ideias, 100")),
    )
}
