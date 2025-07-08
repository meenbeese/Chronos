package com.meenbeese.chronos.data.preference

import android.annotation.SuppressLint

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp

import com.meenbeese.chronos.data.PreferenceEntry

import kotlinx.coroutines.launch

/**
 * Allow the user to choose from a simple boolean
 * using a switch item view.
 */
@Composable
@SuppressLint("ModifierParameter")
fun BooleanPreference(
    preference: PreferenceEntry.BooleanPref,
    @StringRes title: Int,
    @StringRes description: Int? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var isChecked by remember {
        mutableStateOf(preference.get(context))
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(id = title),
                style = MaterialTheme.typography.bodyLarge
            )
            description?.let {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stringResource(id = it),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Switch(
            checked = isChecked,
            onCheckedChange = {
                isChecked = it
                coroutineScope.launch {
                    preference.set(context, it)
                }
            },
            modifier = Modifier
                .padding(start = 12.dp)
        )
    }
}
