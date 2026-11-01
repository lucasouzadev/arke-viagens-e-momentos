package dev.arke.app

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
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
    val fieldColors = TextFieldDefaults.colors(
        focusedLabelColor = MaterialTheme.colorScheme.onSurface,
        cursorColor = MaterialTheme.colorScheme.onSurface,
        focusedIndicatorColor = MaterialTheme.colorScheme.onSurface,
    )
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
    ModalBottomSheet(onDismissRequest = { if (!saving) cancel() }, sheetState = sheet) {
        Column(
            Modifier.fillMaxWidth().imePadding().verticalScroll(rememberScrollState()).padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                Text(
                    stringResource(if (reviewing) R.string.review_place else R.string.add_place),
                    Modifier.weight(1f), style = MaterialTheme.typography.titleLarge,
                )
                TextButton(colors = textButtonColors, onClick = { cancel() }, enabled = !saving, modifier = Modifier.testTag("place.cancel")) {
                    Text(stringResource(R.string.cancel))
                }
            }
            if (reviewing) {
                Text(draft.normalized().name, style = MaterialTheme.typography.headlineSmall)
                Text(draft.normalized().address)
                if (draft.normalized().note.isNotEmpty()) Text(draft.normalized().note)
                Text(stringResource(R.string.place_local_notice), style = MaterialTheme.typography.bodySmall)
                TextButton(colors = textButtonColors, onClick = { reviewing = false }, enabled = !saving, modifier = Modifier.testTag("place.edit")) {
                    Text(stringResource(R.string.edit_information))
                }
                Button(onClick = { onSave(draft.normalized()) }, enabled = !saving, modifier = Modifier.fillMaxWidth().testTag("place.save")) {
                    Text(stringResource(if (saving) R.string.saving else R.string.save_on_device))
                }
            } else {
                TextField(
                    colors = fieldColors,
                    value = draft.name, onValueChange = { draft = draft.copy(name = it) },
                    label = { Text(stringResource(R.string.place_name)) }, singleLine = true, enabled = !saving,
                    modifier = Modifier.fillMaxWidth().testTag("place.name"),
                )
                TextField(
                    colors = fieldColors,
                    value = draft.address, onValueChange = { draft = draft.copy(address = it) },
                    label = { Text(stringResource(R.string.place_address)) }, minLines = 2, maxLines = 4, enabled = !saving,
                    modifier = Modifier.fillMaxWidth().testTag("place.address"),
                )
                TextField(
                    colors = fieldColors,
                    value = draft.note, onValueChange = { draft = draft.copy(note = it) },
                    label = { Text(stringResource(R.string.place_note)) }, minLines = 3, maxLines = 6, enabled = !saving,
                    modifier = Modifier.fillMaxWidth().testTag("place.note"),
                )
                if (submitted) draft.issue?.let { issue ->
                    Text(stringResource(issue.message()), color = MaterialTheme.colorScheme.error, modifier = Modifier.testTag("place.validation"))
                }
                Button(onClick = {
                    submitted = true
                    if (draft.issue == null) { focus.clearFocus(); keyboard?.hide(); reviewing = true }
                }, modifier = Modifier.fillMaxWidth().testTag("place.review")) { Text(stringResource(R.string.review_place)) }
                Text(stringResource(R.string.place_required_notice), style = MaterialTheme.typography.bodySmall)
            }
            if (saveError != null) Text(saveError, color = MaterialTheme.colorScheme.error, modifier = Modifier.testTag("place.saveError"))
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

private fun PlaceDraftIssue.message(): Int = when (this) {
    PlaceDraftIssue.NAME_REQUIRED -> R.string.name_required
    PlaceDraftIssue.NAME_TOO_LONG -> R.string.name_too_long
    PlaceDraftIssue.ADDRESS_REQUIRED -> R.string.address_required
    PlaceDraftIssue.ADDRESS_TOO_LONG -> R.string.address_too_long
    PlaceDraftIssue.NOTE_TOO_LONG -> R.string.note_too_long
}
