package com.meenbeese.chronos.dialogs

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp

import com.meenbeese.chronos.R

@Composable
fun SnoozeDurationDialog(
    names: List<String>,
    onDismiss: () -> Unit,
    onSnoozeSelected: (Int) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(id = android.R.string.cancel))
            }
        },
        title = {
            Text(
                text = stringResource(id = R.string.title_snooze_duration),
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            LazyColumn {
                itemsIndexed(names) { index, name ->
                    Text(
                        text = name,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onSnoozeSelected(index)
                                onDismiss()
                            }
                            .padding(vertical = 12.dp, horizontal = 8.dp),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        },
        modifier = Modifier
            .widthIn(min = 300.dp, max = 600.dp)
            .heightIn(min = 200.dp, max = 400.dp)
    )
}
