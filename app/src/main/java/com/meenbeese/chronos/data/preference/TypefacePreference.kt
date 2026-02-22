package com.meenbeese.chronos.data.preference

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp

import com.meenbeese.chronos.R
import com.meenbeese.chronos.data.Preferences

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * Allow the user to choose the typeface of the application.
 */
@Composable
fun TypefacePreference(
    coroutineScope: CoroutineScope,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val typefaces = stringArrayResource(id = R.array.array_typefaces)
    val currentTypefaceValue = remember { Preferences.TYPEFACE.get(context) }
    var expanded by remember { mutableStateOf(false) }

    var selectedTypefaceIndex by remember { mutableIntStateOf(currentTypefaceValue) }
    var selectedText by remember { mutableStateOf(typefaces.getOrNull(selectedTypefaceIndex) ?: typefaces.first()) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(R.string.title_typeface),
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f)
        )

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            TextField(
                readOnly = true,
                value = selectedText,
                onValueChange = {},
                modifier = Modifier
                    .menuAnchor(type = ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                    .width(180.dp),
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                },
                colors = ExposedDropdownMenuDefaults.textFieldColors(),
                textStyle = MaterialTheme.typography.bodyLarge
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                typefaces.forEachIndexed { index, typefaceName ->
                    DropdownMenuItem(
                        text = { Text(text = typefaceName) },
                        onClick = {
                            expanded = false
                            selectedText = typefaceName
                            selectedTypefaceIndex = index
                            coroutineScope.launch {
                                Preferences.TYPEFACE.set(context, index)
                            }
                        }
                    )
                }
            }
        }
    }
}
