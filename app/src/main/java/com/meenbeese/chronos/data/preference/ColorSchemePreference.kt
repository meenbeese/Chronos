package com.meenbeese.chronos.data.preference

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp

import com.meenbeese.chronos.R
import com.meenbeese.chronos.data.Preferences
import com.meenbeese.chronos.ui.theme.ThemeFactory.catpucchinLatte

import kotlinx.coroutines.runBlocking

@Composable
fun ColorSchemePreference(
    @StringRes nameRes: Int,
    onSelectionChanged: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val options = listOf(
        stringResource(R.string.button_system),
        stringResource(R.string.button_custom)
    )
    var selectedIndex by remember {
        mutableIntStateOf(if (Preferences.DYNAMIC_COLOR.get(context)) 0 else 1)
    }
    var selectedColor by remember {
        mutableIntStateOf(Preferences.COLOR_SEED.get(context))
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(nameRes),
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f)
        )

        SingleChoiceSegmentedButtonRow {
            options.forEachIndexed { index, label ->
                SegmentedButton(
                    shape = SegmentedButtonDefaults.itemShape(index = index, count = options.size),
                    onClick = {
                        selectedIndex = index
                        val dynColor = index == 0
                        runBlocking { Preferences.DYNAMIC_COLOR.set(context, dynColor) }
                        onSelectionChanged()
                    },
                    selected = selectedIndex == index,
                    label = { Text(label) }
                )
            }
        }
    }

    if (selectedIndex == 1) {
        Spacer(modifier = Modifier.height(8.dp))
        CatppuccinColorSelector(
            selectedColor = selectedColor,
            onColorSelected = { newColor ->
                selectedColor = newColor
                runBlocking { Preferences.COLOR_SEED.set(context, newColor) }
                onSelectionChanged()
            }
        )
    }
}

@Composable
fun CatppuccinColorSelector(
    selectedColor: Int,
    onColorSelected: (Int) -> Unit
) {
    val scrollState = rememberScrollState()
    Row(
        modifier = Modifier
            .horizontalScroll(scrollState)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        catpucchinLatte.forEach { colorInt ->
            val color = Color(colorInt)
            val isSelected = selectedColor == colorInt

            Box(
                modifier = Modifier
                    .padding(end = 16.dp)
                    .size(if (isSelected) 40.dp else 32.dp)
                    .clip(MaterialTheme.shapes.small)
                    .background(color)
                    .clickable { onColorSelected(colorInt) }
                    .then(
                        if (isSelected) Modifier.border(
                            2.dp,
                            MaterialTheme.colorScheme.primary,
                            MaterialTheme.shapes.small
                        ) else Modifier
                    )
            )
        }
    }
}
