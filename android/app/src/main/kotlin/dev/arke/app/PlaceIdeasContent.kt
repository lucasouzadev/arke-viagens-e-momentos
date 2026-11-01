package dev.arke.app

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
