package com.meenbeese.chronos.data.preference

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

import com.meenbeese.chronos.ui.screens.FileChooserScreen
import com.meenbeese.chronos.ui.screens.FileChooserType

@Composable
fun ImportExportPreference() {
    var showChooser by remember { mutableStateOf(false) }
    var chooserType by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(36.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedButton(onClick = {
                chooserType = FileChooserType.IMPORT_JSON
                showChooser = true
            }) {
                Text(
                    text = "Import Alarms",
                    style = MaterialTheme.typography.labelMedium
                )
            }

            OutlinedButton(onClick = {
                chooserType = FileChooserType.EXPORT_JSON
                showChooser = true
            }) {
                Text(
                    text = "Export Alarms",
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }
    }

    if (showChooser) {
        FileChooserScreen(
            type = chooserType,
            preference = null,
            onFileChosen = { _, _, -> showChooser = false },
            onDismiss = { showChooser = false }
        )
    }
}
