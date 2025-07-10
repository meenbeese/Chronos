package com.meenbeese.chronos.data.preference

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.graphics.toColorInt

import com.github.skydoves.colorpicker.compose.HsvColorPicker
import com.github.skydoves.colorpicker.compose.rememberColorPickerController
import com.meenbeese.chronos.R
import com.meenbeese.chronos.data.PreferenceEntry

import kotlinx.coroutines.launch

@Composable
fun ColorPreference(
    preference: PreferenceEntry.IntPref,
    @StringRes title: Int,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val hexRegex = Regex("^[0-9A-Fa-f]{6}$")
    val coroutineScope = rememberCoroutineScope()
    val controller = rememberColorPickerController()

    var showDialog by remember {
        mutableStateOf(false)
    }
    var currentColor by remember {
        mutableIntStateOf(preference.get(context))
    }
    var hexInput by remember {
        mutableStateOf(String.format("%06X", 0xFFFFFF and currentColor))
    }
    var isHexValid by remember(hexInput) {
        mutableStateOf(hexInput.matches(hexRegex))
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable { showDialog = true }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(id = title),
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f)
        )

        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(Color(currentColor))
                .border(1.dp, Color.Gray, RoundedCornerShape(6.dp))
        )
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            confirmButton = {
                TextButton(onClick = {
                    showDialog = false
                    coroutineScope.launch {
                        preference.set(context, currentColor)
                    }
                }) {
                    Text("Ok")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Cancel")
                }
            },
            title = { Text(text = stringResource(id = R.string.title_picker_dialog)) },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    HsvColorPicker(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(400.dp),
                        controller = controller,
                        initialColor = Color(currentColor),
                        onColorChanged = { envelope ->
                            currentColor = envelope.color.toArgb()
                            hexInput = String.format("%06X", 0xFFFFFF and currentColor)
                        }
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(currentColor))
                                .border(1.dp, Color.Gray, RoundedCornerShape(6.dp))
                        )

                        OutlinedTextField(
                            value = hexInput,
                            onValueChange = {
                                hexInput = it.uppercase()
                                if (hexInput.matches(hexRegex)) {
                                    val parsedColor = Color("#$hexInput".toColorInt())
                                    currentColor = parsedColor.toArgb()
                                    controller.selectByColor(parsedColor, true)
                                }
                            },
                            label = { Text("Hex Color") },
                            isError = !isHexValid,
                            leadingIcon = { Text("#") },
                            keyboardOptions = KeyboardOptions.Default.copy(
                                keyboardType = KeyboardType.Ascii,
                                imeAction = ImeAction.Done
                            ),
                            modifier = Modifier
                                .padding(start = 12.dp)
                                .weight(1f)
                        )
                    }
                }
            }
        )
    }
}
