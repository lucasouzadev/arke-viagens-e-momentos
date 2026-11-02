package dev.arke.app

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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

private val DraftSaver = Saver<PlaceDraft, List<String>>(
    save = { listOf(it.id, it.name, it.address, it.note) },
    restore = { PlaceDraft(it[0], it[1], it[2], it[3]) },
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaceEditor(saving: Boolean, saveError: String?, onSave: (PlaceDraft) -> Unit, onCancel: () -> Unit) {
    var draft by rememberSaveable(stateSaver = DraftSaver) { mutableStateOf(PlaceDraft()) }
    var reviewing by rememberSaveable { mutableStateOf(false) }
    var submitted by rememberSaveable { mutableStateOf(false) }
    var confirmingDiscard by remember { mutableStateOf(false) }
    val actionColor = if (isSystemInDarkTheme()) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.primary
    val textButtonColors = ButtonDefaults.textButtonColors(contentColor = actionColor)
    val sheetColor = arkeSheetColor()
    val focus = LocalFocusManager.current
    val keyboard = LocalSoftwareKeyboardController.current
    fun cancel() {
        focus.clearFocus()
        keyboard?.hide()
        if (draft.isEmpty) onCancel() else confirmingDiscard = true
    }
    val sheet = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
        confirmValueChange = { it != SheetValue.Hidden || (draft.isEmpty && !saving) },
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
                    modifier = Modifier.width(76.dp).heightIn(min = 48.dp).testTag("place.cancel"),
                ) { Text(stringResource(R.string.cancel), Modifier.fillMaxWidth(), textAlign = TextAlign.Start) }
                Text(
                    stringResource(if (reviewing) R.string.review_place else R.string.add_place),
                    Modifier.weight(1f).testTag("place.title").semantics { heading() },
                    style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center,
                )
                Spacer(Modifier.width(76.dp))
            }
            Column(
                Modifier.fillMaxWidth().weight(1f)
                    .drawWithContent {
                        drawContent()
                        drawRect(Brush.verticalGradient(
                            colors = listOf(Color.Transparent, sheetColor),
                            startY = (size.height - 32.dp.toPx()).coerceAtLeast(0f),
                            endY = size.height,
                        ))
                    }
                    .verticalScroll(rememberScrollState()).testTag("place.fields")
                    .padding(top = 24.dp, bottom = 48.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text(stringResource(if (reviewing) R.string.check_idea else R.string.place_section), style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                if (reviewing) {
                    PlaceSummary(stringResource(R.string.place_name), draft.normalized().name, 56.dp)
                    PlaceSummary(stringResource(R.string.place_address), draft.normalized().address, 96.dp)
                    PlaceSummary(stringResource(R.string.place_note), draft.normalized().note.ifEmpty { stringResource(R.string.no_note) }, 120.dp)
                } else {
                    PlaceInput(stringResource(R.string.place_name), draft.name, { draft = draft.copy(name = it) }, 56.dp, 1, 1, saving, "place.name")
                    PlaceInput(stringResource(R.string.place_address), draft.address, { draft = draft.copy(address = it) }, 96.dp, 2, 4, saving, "place.address")
                    PlaceInput(stringResource(R.string.place_note), draft.note, { draft = draft.copy(note = it) }, 120.dp, 3, 6, saving, "place.note")
                    if (submitted) draft.issue?.let { issue ->
                        Text(stringResource(issue.message()), style = MaterialTheme.typography.bodySmall, modifier = Modifier.testTag("place.validation"))
                    }
                }
                if (saveError != null) Text(saveError, style = MaterialTheme.typography.bodySmall, modifier = Modifier.testTag("place.saveError"))
            }
            Column(Modifier.fillMaxWidth().padding(bottom = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                if (reviewing) {
                    TextButton(
                        colors = textButtonColors, onClick = { reviewing = false }, enabled = !saving,
                        modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp).testTag("place.edit"),
                    ) { Text(stringResource(R.string.edit_information)) }
                    Button(
                        onClick = { onSave(draft.normalized()) }, enabled = !saving, shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp).testTag("place.save"),
                    ) { Text(stringResource(if (saving) R.string.saving else R.string.save_on_device)) }
                    Text(stringResource(R.string.place_local_notice), style = MaterialTheme.typography.bodySmall)
                } else {
                    Button(onClick = {
                        submitted = true
                        if (draft.issue == null) { focus.clearFocus(); keyboard?.hide(); reviewing = true }
                    }, enabled = !saving, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp).testTag("place.review")) {
                        Text(stringResource(R.string.review_place))
                    }
                    Text(stringResource(R.string.place_required_notice), style = MaterialTheme.typography.bodySmall)
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
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(label, style = MaterialTheme.typography.labelMedium)
        BasicTextField(
            value = value, onValueChange = onChange, enabled = !saving,
            singleLine = maxLines == 1, minLines = minLines, maxLines = maxLines,
            textStyle = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurface),
            cursorBrush = SolidColor(MaterialTheme.colorScheme.onSurface),
            keyboardOptions = KeyboardOptions(capitalization = if (maxLines == 1) KeyboardCapitalization.Words else KeyboardCapitalization.Sentences, imeAction = if (maxLines == 1) ImeAction.Next else ImeAction.Default),
            keyboardActions = KeyboardActions(onNext = { focus.moveFocus(FocusDirection.Down) }),
            modifier = Modifier.fillMaxWidth().heightIn(min = minHeight)
                .background(arkeInsetColor(), RoundedCornerShape(12.dp))
                .padding(16.dp).testTag(tag).semantics { contentDescription = label },
        )
    }
}

@Composable
private fun PlaceSummary(label: String, value: String, minHeight: Dp) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(label, style = MaterialTheme.typography.labelMedium)
        Text(value, style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.fillMaxWidth().heightIn(min = minHeight).background(arkeInsetColor(), RoundedCornerShape(12.dp)).padding(16.dp))
    }
}

private fun PlaceDraftIssue.message(): Int = when (this) {
    PlaceDraftIssue.NAME_REQUIRED -> R.string.name_required
    PlaceDraftIssue.NAME_TOO_LONG -> R.string.name_too_long
    PlaceDraftIssue.ADDRESS_REQUIRED -> R.string.address_required
    PlaceDraftIssue.ADDRESS_TOO_LONG -> R.string.address_too_long
    PlaceDraftIssue.NOTE_TOO_LONG -> R.string.note_too_long
}
