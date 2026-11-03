package dev.arke.app

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia
import androidx.compose.foundation.Image
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.arke.core.PlaceDraft
import dev.arke.core.PlaceDraftIssue
import dev.arke.core.PlaceIdea

private val DraftSaver = Saver<PlaceDraft, List<String>>(
    save = { listOf(it.id, it.name, it.address, it.note, it.link, it.cover) },
    restore = { PlaceDraft(it[0], it[1], it[2], it[3], it.getOrElse(4) { "" }, it.getOrElse(5) { "" }) },
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaceEditor(saving: Boolean, saveError: String?, onSave: (PlaceDraft) -> Unit, onCancel: () -> Unit, initialIdea: PlaceIdea? = null) {
    val baseline = remember(initialIdea) { initialIdea?.let { PlaceDraft(it.id, it.name, it.address, it.note, it.link, it.cover) } ?: PlaceDraft() }
    var draft by rememberSaveable(stateSaver = DraftSaver) { mutableStateOf(baseline) }
    val hasChanges = if (initialIdea == null) !draft.isEmpty else draft.normalized() != baseline.normalized()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var loadingCover by remember { mutableStateOf(false) }
    var coverError by remember { mutableStateOf(false) }
    val picker = rememberLauncherForActivityResult(PickVisualMedia()) { uri ->
        if (uri != null) {
            loadingCover = true
            coverError = false
            scope.launch {
                try {
                    val cover = withContext(Dispatchers.IO) { readPlaceCover(context.contentResolver, uri) }
                    if (cover == null) coverError = true else draft = draft.copy(cover = cover)
                } finally {
                    loadingCover = false
                }
            }
        }
    }
    var reviewing by rememberSaveable { mutableStateOf(false) }
    var submitted by rememberSaveable { mutableStateOf(false) }
    var confirmingDiscard by remember { mutableStateOf(false) }
    var showingInfo by remember { mutableStateOf(false) }
    val actionColor = if (isSystemInDarkTheme()) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.primary
    val textButtonColors = ButtonDefaults.textButtonColors(contentColor = actionColor)
    val focus = LocalFocusManager.current
    val keyboard = LocalSoftwareKeyboardController.current
    fun cancel() {
        focus.clearFocus()
        keyboard?.hide()
        if (!saving) { if (!hasChanges) onCancel() else confirmingDiscard = true }
    }
    val canDismiss by rememberUpdatedState(!hasChanges && !saving)
    val confirmSheetChange = remember { { value: SheetValue -> value != SheetValue.Hidden || canDismiss } }
    val sheet = rememberModalBottomSheetState(
        skipPartiallyExpanded = false,
        confirmValueChange = confirmSheetChange,
    )
    ArkeFloatingSheet(
        onDismiss = { if (!saving) cancel() }, tagPrefix = "place", sheetState = sheet, enabled = !saving,
        modifier = Modifier.fillMaxHeight(0.9f).imePadding(),
    ) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp).padding(top = 16.dp)) {
            Row(Modifier.fillMaxWidth().heightIn(min = 48.dp), verticalAlignment = Alignment.CenterVertically) {
                TextButton(
                    colors = textButtonColors, onClick = { cancel() }, enabled = !saving,
                    contentPadding = PaddingValues(0.dp),
                    shape = CircleShape,
                    modifier = Modifier.width(48.dp).heightIn(min = 48.dp).testTag("place.cancel"),
                ) {
                    Box(Modifier.size(48.dp).background(arkeInsetColor(), CircleShape), contentAlignment = Alignment.Center) {
                        Icon(painterResource(R.drawable.ic_close), stringResource(R.string.cancel))
                    }
                }
                Spacer(Modifier.width(28.dp))
                Text(
                    stringResource(if (reviewing) R.string.review_place else if (initialIdea == null) R.string.add_place else R.string.edit_place),
                    Modifier.weight(1f).testTag("place.title").semantics { heading() },
                    style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center,
                )
                Box(Modifier.width(76.dp), contentAlignment = Alignment.CenterEnd) {
                    IconButton(onClick = { focus.clearFocus(); keyboard?.hide(); showingInfo = true },
                        modifier = Modifier.size(48.dp).background(arkeInsetColor(), CircleShape).testTag("place.info")) {
                        Icon(painterResource(R.drawable.ic_info), "Informações sobre salvar lugares")
                    }
                    DropdownMenu(expanded = showingInfo, onDismissRequest = { showingInfo = false }) {
                        Column(Modifier.width(280.dp).padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text("Sobre sua ideia", style = MaterialTheme.typography.titleMedium)
                            Text("Nome e endereço são obrigatórios. A nota é opcional. A ideia fica só neste aparelho, sem backup; desinstalar o app pode apagá-la. O endereço não será marcado no mapa. Você pode salvar sem criar uma viagem.", modifier = Modifier.testTag("place.infoText"))
                            TextButton(onClick = { showingInfo = false }, modifier = Modifier.heightIn(min = 48.dp).testTag("place.infoDismiss")) { Text("Entendi") }
                        }
                    }
                }
            }
            Column(
                Modifier.fillMaxWidth().weight(1f)
                    .verticalScroll(rememberScrollState()).testTag("place.fields")
                    .padding(top = 16.dp, bottom = 12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Column(Modifier.fillMaxWidth().background(arkeInsetColor(), RoundedCornerShape(24.dp)).padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)) {
                val cover = remember(draft.cover) { decodePlaceCover(draft.cover) }
                if (cover != null) {
                    Image(cover.asImageBitmap(), contentDescription = null,
                        modifier = Modifier.fillMaxWidth().height(112.dp), contentScale = ContentScale.Crop)
                }
                if (!reviewing) {
                    TextButton(enabled = !saving && !loadingCover, onClick = {
                        picker.launch(PickVisualMediaRequest(PickVisualMedia.ImageOnly))
                    }) { Text(if (cover == null) "Adicionar foto de capa" else "Trocar foto de capa") }
                    if (cover != null) {
                        TextButton(enabled = !saving && !loadingCover, onClick = {
                            draft = draft.copy(cover = "")
                            coverError = false
                        }) { Text("Remover foto") }
                    }
                    if (loadingCover) Text("Carregando foto…")
                    if (coverError) Text("Não foi possível abrir esta foto.", color = MaterialTheme.colorScheme.error)
                }
                if (reviewing) {
                    PlaceSummary(stringResource(R.string.place_name), draft.normalized().name, 56.dp)
                    PlaceSummary(stringResource(R.string.place_address), draft.normalized().address, 96.dp)
                    if (draft.link.isNotBlank()) PlaceSummary("Link", draft.link.trim(), 48.dp)
                    PlaceSummary(stringResource(R.string.place_note), draft.normalized().note.ifEmpty { stringResource(R.string.no_note) }, 120.dp)
                } else {
                    PlaceInput(stringResource(R.string.place_name), draft.name, { draft = draft.copy(name = it) }, 56.dp, 1, 1, saving, "place.name")
                    HorizontalDivider()
                    PlaceInput(stringResource(R.string.place_address), draft.address, { draft = draft.copy(address = it) }, 96.dp, 2, 4, saving, "place.address")
                    HorizontalDivider()
                    PlaceInput(stringResource(R.string.place_note), draft.note, { draft = draft.copy(note = it) }, 120.dp, 3, 6, saving, "place.note")
                    HorizontalDivider()
                    PlaceInput("Link opcional", draft.link, { draft = draft.copy(link = it) }, 48.dp, 1, 1, saving, "place.link")
                    if (submitted) draft.issue?.let { issue ->
                        Text(stringResource(issue.message()), style = MaterialTheme.typography.bodySmall, modifier = Modifier.testTag("place.validation"))
                    }
                }
                }
                if (saveError != null) Text(saveError, style = MaterialTheme.typography.bodySmall, modifier = Modifier.testTag("place.saveError"))
            Column(Modifier.fillMaxWidth().padding(bottom = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                if (reviewing) {
                    TextButton(
                        colors = textButtonColors, onClick = { reviewing = false }, enabled = !saving,
                        modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp).background(arkeInsetColor(), CircleShape).testTag("place.edit"),
                    ) { Text(stringResource(R.string.edit_information)) }
                    Button(
                        onClick = { onSave(draft.normalized()) }, enabled = !saving, shape = CircleShape,
                        modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp).testTag("place.save"),
                    ) { Text(stringResource(if (saving) R.string.saving else if (initialIdea == null) R.string.save_on_device else R.string.save_changes)) }
                } else {
                    Button(onClick = {
                        submitted = true
                        if (draft.issue == null) { focus.clearFocus(); keyboard?.hide(); reviewing = true }
                    }, enabled = !saving && !loadingCover, shape = CircleShape, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp).testTag("place.review")) {
                        Text(stringResource(R.string.review_place))
                    }
                }
            }
            }
        }
    }
    if (confirmingDiscard) {
        AlertDialog(
            onDismissRequest = { confirmingDiscard = false },
            title = { Text(stringResource(R.string.discard_draft_title)) },
            text = { Text(stringResource(R.string.discard_draft_detail)) },
            confirmButton = { TextButton(colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error), onClick = onCancel, modifier = Modifier.testTag("place.discard")) { Text(stringResource(R.string.discard)) } },
            dismissButton = { TextButton(colors = textButtonColors, onClick = { confirmingDiscard = false }) { Text(stringResource(R.string.keep_editing)) } },
        )
    }
}

@Composable
private fun PlaceInput(label: String, value: String, onChange: (String) -> Unit, minHeight: Dp, minLines: Int, maxLines: Int, saving: Boolean, tag: String) {
    val focus = LocalFocusManager.current
    val requester = remember { FocusRequester() }
    Column(Modifier.clickable(enabled = !saving) { requester.requestFocus() }, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(painterResource(if (label == stringResource(R.string.place_name)) R.drawable.ic_bookmark else if (label == stringResource(R.string.place_address)) R.drawable.ic_pin else R.drawable.ic_note), null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f), modifier = Modifier.size(24.dp))
            Text(label, style = MaterialTheme.typography.labelMedium)
        }
        BasicTextField(
            value = value, onValueChange = onChange, enabled = !saving,
            singleLine = maxLines == 1, minLines = minLines, maxLines = maxLines,
            textStyle = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurface),
            cursorBrush = SolidColor(MaterialTheme.colorScheme.onSurface),
            keyboardOptions = KeyboardOptions(capitalization = if (maxLines == 1) KeyboardCapitalization.Words else KeyboardCapitalization.Sentences, imeAction = if (maxLines == 1) ImeAction.Next else ImeAction.Default),
            keyboardActions = KeyboardActions(onNext = { focus.moveFocus(FocusDirection.Down) }),
            modifier = Modifier.fillMaxWidth().heightIn(min = minHeight).padding(start = 36.dp, end = 4.dp)
                .focusRequester(requester).testTag(tag).semantics { contentDescription = label },
        )
    }
}

@Composable
private fun PlaceSummary(label: String, value: String, minHeight: Dp) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(painterResource(if (label == stringResource(R.string.place_name)) R.drawable.ic_bookmark else if (label == stringResource(R.string.place_address)) R.drawable.ic_pin else R.drawable.ic_note), null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f), modifier = Modifier.size(24.dp))
            Text(label, style = MaterialTheme.typography.labelMedium)
        }
        Text(value, style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.fillMaxWidth().heightIn(min = minHeight))
    }
}

private fun PlaceDraftIssue.message(): Int = when (this) {
    PlaceDraftIssue.NAME_REQUIRED -> R.string.name_required
    PlaceDraftIssue.NAME_TOO_LONG -> R.string.name_too_long
    PlaceDraftIssue.ADDRESS_REQUIRED -> R.string.address_required
    PlaceDraftIssue.ADDRESS_TOO_LONG -> R.string.address_too_long
    PlaceDraftIssue.INVALID_LINK -> R.string.invalid_place_link
    PlaceDraftIssue.NOTE_TOO_LONG -> R.string.note_too_long
}

internal fun decodePlaceCover(encoded: String): Bitmap? {
    if (encoded.isEmpty() || encoded.length > 88_000) return null
    return runCatching {
        val bytes = Base64.decode(encoded, Base64.NO_WRAP)
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    }.getOrNull()
}

private fun readPlaceCover(resolver: android.content.ContentResolver, uri: Uri): String? {
    return runCatching {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bounds) }
        val largest = maxOf(bounds.outWidth, bounds.outHeight)
        if (largest <= 0) return null
        val options = BitmapFactory.Options()
        while (largest / options.inSampleSize.coerceAtLeast(1) > 960) {
            options.inSampleSize = options.inSampleSize.coerceAtLeast(1) * 2
        }
        val bitmap = resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, options) } ?: return null
        val output = ByteArrayOutputStream()
        for (quality in listOf(68, 48, 30)) {
            output.reset()
            if (bitmap.compress(Bitmap.CompressFormat.JPEG, quality, output) && output.size() <= 65_000) {
                return Base64.encodeToString(output.toByteArray(), Base64.NO_WRAP)
            }
        }
        null
    }.getOrNull()
}
