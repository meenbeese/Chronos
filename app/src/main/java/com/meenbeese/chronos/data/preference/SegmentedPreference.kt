package com.meenbeese.chronos.data.preference

import androidx.annotation.StringRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
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
    var isColorSelected by remember {
        mutableStateOf(Preferences.COLORFUL_BACKGROUND.get(context))
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = stringResource(nameRes),
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp)
        ) {
            SegmentedOption(
                text = stringResource(R.string.button_color),
                selected = isColorSelected,
                modifier = Modifier.weight(1f)
            ) {
                isColorSelected = true
                runBlocking { Preferences.COLORFUL_BACKGROUND.set(context, true) }
                onSelectionChanged()
            }

            Spacer(modifier = Modifier.width(8.dp))

            SegmentedOption(
                text = stringResource(R.string.button_image),
                selected = !isColorSelected,
                modifier = Modifier.weight(1f)
            ) {
                isColorSelected = false
                runBlocking { Preferences.COLORFUL_BACKGROUND.set(context, false) }
                onSelectionChanged()
            }
        }
    }
}

@Composable
fun SegmentedOption(
    text: String,
    selected: Boolean,
    modifier: Modifier,
    onClick: () -> Unit
) {
    val background = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface
    val contentColor = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface

    Surface(
        color = background,
        shape = RoundedCornerShape(50),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        modifier = modifier
            .clickable { onClick() }
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp)
        ) {
            Text(
                text = text,
                color = contentColor,
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}
