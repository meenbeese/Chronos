package com.meenbeese.chronos.data.preference

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp

import com.meenbeese.chronos.R
import com.meenbeese.chronos.data.Preferences

import kotlinx.coroutines.runBlocking

@Composable
fun SegmentedPreference(
    @StringRes nameRes: Int,
    onSelectionChanged: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val options = listOf(
        stringResource(R.string.button_color),
        stringResource(R.string.button_image)
    )
    var selectedIndex by remember {
        mutableIntStateOf(if (Preferences.COLORFUL_BACKGROUND.get(context)) 0 else 1)
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
                        val useColor = index == 0
                        runBlocking { Preferences.COLORFUL_BACKGROUND.set(context, useColor) }
                        onSelectionChanged()
                    },
                    selected = selectedIndex == index,
                    label = { Text(label) }
                )
            }
        }
    }
}
