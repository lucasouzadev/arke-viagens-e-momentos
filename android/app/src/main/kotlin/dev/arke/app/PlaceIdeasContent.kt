package dev.arke.app

import androidx.compose.foundation.layout.*
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.text.style.TextAlign
import dev.arke.core.PlaceDraft
import androidx.compose.material3.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.horizontalScroll
import dev.arke.core.PanelDetent
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
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
    onDetailsVisibilityChanged: (Boolean) -> Unit = {}, onFocus: (String?) -> Unit, onPin: (PlaceIdea) -> Unit, detent: PanelDetent, onDelete: (String) -> Unit) {
    var focusedID by rememberSaveable { mutableStateOf<String?>(null) }
    var deleting by remember { mutableStateOf<PlaceIdea?>(null) }
    var selectedID by rememberSaveable { mutableStateOf<String?>(null) }
    var editing by rememberSaveable(stateSaver = IdeaSaver) { mutableStateOf<PlaceIdea?>(null) }
    var pendingUpdate by rememberSaveable(stateSaver = PendingDraftSaver) { mutableStateOf<PlaceDraft?>(null) }
    var pendingRequest by rememberSaveable { mutableStateOf<String?>(null) }
    LaunchedEffect(state.ideas, focusedID) {
        onFocus(state.ideas.firstOrNull { it.id == focusedID }?.id ?: state.ideas.firstOrNull()?.id)
    }
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
    } else if (detent == PanelDetent.EXPLORE) {
        Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            state.ideas.forEach { idea ->
                TextButton(onClick = { focusedID = idea.id; selectedID = idea.id }) {
                    Column(Modifier.width(80.dp)) {
                        val thumbnail = remember(idea.cover) { decodePlaceCover(idea.cover) }
                        if (thumbnail != null) Image(thumbnail.asImageBitmap(), null,
                            Modifier.fillMaxWidth().height(50.dp).clip(RoundedCornerShape(8.dp)), contentScale = ContentScale.Crop)
                        else Text("◉", Modifier.height(50.dp))
                        Text(idea.name, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
                    }
                }
            }
        }
    } else {
        val index = state.ideas.indexOfFirst { it.id == focusedID }.coerceAtLeast(0)
        val idea = state.ideas[index]
        val previous = state.ideas.getOrNull(index - 1)
        val next = state.ideas.getOrNull(index + 1)
        key(idea.id) {
            val shape = RoundedCornerShape(26.dp)
            val cover = remember(idea.cover) { decodePlaceCover(idea.cover) }
            val ink = if (isSystemInDarkTheme()) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.primary
            // Cartão do lugar como no protótipo: selo, nome, endereço, posição, nota, Detalhes e Marcar no mapa.
            Box(Modifier.fillMaxWidth().clip(shape).background(arkeInsetColor()).pointerInput(previous?.id, next?.id) {
                var distance = 0f
                detectHorizontalDragGestures(
                    onDragStart = { distance = 0f },
                    onDragEnd = {
                        val target = if (distance < -28.dp.toPx()) next else if (distance > 28.dp.toPx()) previous else null
                        if (target != null) focusedID = target.id
                    },
                    onHorizontalDrag = { _, delta -> distance += delta },
                )
            }) {
                Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.Top) {
                        Box(Modifier.size(76.dp).clip(RoundedCornerShape(12.dp)).background(arkeSheetColor()), contentAlignment = Alignment.Center) {
                            if (cover != null) Image(cover.asImageBitmap(), null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                            else Icon(painterResource(R.drawable.ic_photo), null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Column(Modifier.weight(1f)) {
                            Text(idea.name, style = MaterialTheme.typography.titleMedium, maxLines = 1,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis, modifier = Modifier.testTag("home.ideaName"))
                            Text(idea.address, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
                            Text("Lugar ${index + 1} de ${state.ideas.size}", style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.outline, modifier = Modifier.padding(top = 2.dp))
                        }
                    }
                    if (idea.note.isNotEmpty()) Text(idea.note, maxLines = 2, color = MaterialTheme.colorScheme.onSurfaceVariant,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        val openLabel = stringResource(R.string.view_place_details_label, idea.name)
                        TextButton(onClick = { selectedID = idea.id },
                            colors = ButtonDefaults.textButtonColors(contentColor = ink),
                            modifier = Modifier.weight(1f).heightIn(min = 48.dp).background(arkeSheetColor(), CircleShape)
                                .testTag("place.open.${idea.id}").semantics { contentDescription = openLabel }) { Text("Detalhes") }
                        TextButton(onClick = { onPin(idea) }, colors = ButtonDefaults.textButtonColors(contentColor = ink),
                            modifier = Modifier.weight(1f).heightIn(min = 48.dp).testTag("place.pin.${idea.id}")) {
                            Icon(painterResource(R.drawable.ic_pin), null, Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Marcar no mapa")
                        }
                    }
                }
            }
        }
        // Navegação finita entre lugares: anterior, contador e próximo.
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            IconButton(onClick = { previous?.let { focusedID = it.id } }, enabled = previous != null, modifier = Modifier.size(48.dp).testTag("home.placePrevious")) {
                Icon(painterResource(R.drawable.ic_back), "Lugar anterior")
            }
            Text("${index + 1} de ${state.ideas.size}", color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.testTag("home.placeCounter"))
            IconButton(onClick = { next?.let { focusedID = it.id } }, enabled = next != null, modifier = Modifier.size(48.dp).testTag("home.placeNext")) {
                Icon(painterResource(R.drawable.ic_back), "Próximo lugar", Modifier.graphicsLayer { rotationZ = 180f })
            }
        }
    }
    Spacer(Modifier.height(24.dp))
    selectedID?.let { id ->
        PlaceDetails(state.ideas, id, onClose = { selectedID = null },
            onEdit = { target -> selectedID = null; pendingUpdate = null; pendingRequest = null; editing = target },
            onPin = { target -> selectedID = null; onPin(target) },
            onDelete = { target -> selectedID = null; deleting = target })
    }
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
    save = { idea -> idea?.let { listOf(it.id, it.name, it.address, it.note, it.link, it.cover) } },
    restore = { PlaceIdea(it[0], it[1], it[2], it[3], it.getOrElse(4) { "" }, it.getOrElse(5) { "" }) },
)

private val PendingDraftSaver = Saver<PlaceDraft?, List<String>>(
    save = { draft -> draft?.let { listOf(it.id, it.name, it.address, it.note, it.link, it.cover) } },
    restore = { PlaceDraft(it[0], it[1], it[2], it[3], it.getOrElse(4) { "" }, it.getOrElse(5) { "" }) },
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaceDetails(ideas: List<PlaceIdea>, initialID: String, onClose: () -> Unit,
                 onEdit: ((PlaceIdea) -> Unit)? = null, onPin: ((PlaceIdea) -> Unit)? = null, onDelete: ((PlaceIdea) -> Unit)? = null) {
    var selectedID by rememberSaveable(initialID) { mutableStateOf(initialID) }
    val index = ideas.indexOfFirst { it.id == selectedID }
    val sheetColor = arkeSheetColor()
    val insetColor = arkeInsetColor()
    val ink = MaterialTheme.colorScheme.onSurface
    val maximumHeight = with(LocalDensity.current) { (LocalWindowInfo.current.containerSize.height * 0.9f).toDp() }
    ArkeFloatingSheet(onDismiss = onClose, tagPrefix = "detail", modifier = Modifier.heightIn(max = maximumHeight)) {
        // Sheet contínuo: o nome é o título e rola com o conteúdo; navegação flutua sobre ele.
        Box(Modifier.fillMaxWidth()) {
            key(selectedID) {
                val detailScroll = rememberScrollState()
                Column(Modifier.fillMaxWidth().verticalScroll(detailScroll).testTag("detail.body")
                    .padding(horizontal = 20.dp).padding(top = 28.dp, bottom = 96.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    if (index >= 0) {
                        val idea = ideas[index]
                        val positionLabel = stringResource(R.string.place_position, index + 1, ideas.size)
                        Text(idea.name, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold,
                            modifier = Modifier.fillMaxWidth().testTag("detail.name").semantics {
                                heading(); liveRegion = LiveRegionMode.Polite; stateDescription = positionLabel
                            })
                        Column(Modifier.fillMaxWidth().background(insetColor, RoundedCornerShape(24.dp)).padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(20.dp)) {
                            DetailContentRow(idea.address, R.drawable.ic_pin, stringResource(R.string.place_address), "detail.address")
                            dev.arke.core.webLink(idea.link)?.let { link ->
                                val uriHandler = androidx.compose.ui.platform.LocalUriHandler.current
                                TextButton(onClick = { runCatching { uriHandler.openUri(link) } }) { Text("Abrir link") }
                            }
                            if (idea.note.isNotEmpty()) DetailContentRow(idea.note, R.drawable.ic_note, stringResource(R.string.place_note), "detail.note")
                        }
                        if (onEdit != null && onPin != null && onDelete != null) {
                            // Ações do lugar (protótipo): Editar, No mapa e Excluir.
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                TextButton(onClick = { onEdit(idea) }, colors = ButtonDefaults.textButtonColors(contentColor = ink),
                                    modifier = Modifier.weight(1f).heightIn(min = 48.dp).background(insetColor, CircleShape).testTag("detail.edit")) {
                                    Icon(painterResource(R.drawable.ic_edit), null, Modifier.size(18.dp)); Spacer(Modifier.width(6.dp)); Text("Editar")
                                }
                                TextButton(onClick = { onPin(idea) }, colors = ButtonDefaults.textButtonColors(contentColor = ink),
                                    modifier = Modifier.weight(1f).heightIn(min = 48.dp).background(insetColor, CircleShape).testTag("detail.pin")) {
                                    Icon(painterResource(R.drawable.ic_pin), null, Modifier.size(18.dp)); Spacer(Modifier.width(6.dp)); Text("No mapa")
                                }
                                TextButton(onClick = { onDelete(idea) }, colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
                                    modifier = Modifier.weight(1f).heightIn(min = 48.dp).background(insetColor, CircleShape).testTag("detail.delete")) {
                                    Icon(painterResource(R.drawable.ic_delete), null, Modifier.size(18.dp)); Spacer(Modifier.width(6.dp)); Text("Excluir")
                                }
                            }
                        }
                        Text(stringResource(R.string.place_detail_notice), style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(horizontal = 4.dp).testTag("detail.infoText"))
                    } else Text(stringResource(R.string.place_unavailable))
                }
            }
            Box(Modifier.align(Alignment.BottomCenter).fillMaxWidth().height(96.dp)
                .background(Brush.verticalGradient(listOf(Color.Transparent, sheetColor))))
            Row(Modifier.align(Alignment.BottomCenter).fillMaxWidth().padding(horizontal = 20.dp).padding(bottom = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                if (index > 0) {
                    IconButton(onClick = { selectedID = ideas[index - 1].id },
                        modifier = Modifier.size(56.dp).shadow(6.dp, CircleShape).background(insetColor, CircleShape).testTag("detail.previous")) {
                        Icon(painterResource(R.drawable.ic_back), stringResource(R.string.previous_place), tint = ink)
                    }
                }
                val isLast = index < 0 || index == ideas.lastIndex
                Button(onClick = { if (isLast) onClose() else selectedID = ideas[index + 1].id }, shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(containerColor = ArkeColors.brand, contentColor = Color.White),
                    modifier = Modifier.weight(1f).heightIn(min = 56.dp).shadow(6.dp, CircleShape).testTag(if (isLast) "detail.done" else "detail.next")) {
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

internal fun Modifier.arkeStamp(paper: Color): Modifier = drawBehind {
    val radius = 4.dp.toPx()
    val edge = Path().apply {
        fillType = PathFillType.EvenOdd
        addRect(Rect(0f, 0f, size.width, size.height))
        fun perforate(x: Float, y: Float) {
            addOval(Rect(x - radius, y - radius, x + radius, y + radius))
        }
        val spacing = 14.dp.toPx()
        for (column in 1 until (size.width / spacing).toInt()) {
            perforate(column * spacing, 0f)
            perforate(column * spacing, size.height)
        }
        for (row in 1 until (size.height / spacing).toInt()) {
            perforate(0f, row * spacing)
            perforate(size.width, row * spacing)
        }
    }
    clipRect { drawPath(edge, paper) }
}
