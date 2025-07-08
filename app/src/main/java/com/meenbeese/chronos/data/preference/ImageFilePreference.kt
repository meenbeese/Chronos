package com.meenbeese.chronos.data.preference

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope

import com.meenbeese.chronos.data.PreferenceEntry
import com.meenbeese.chronos.views.PreferenceItem

import kotlinx.coroutines.launch

/**
 * A preference item that allows the user to select
 * an image from a file (the resulting preference
 * contains a valid image path / URI).
 */
@Composable
fun ImageFilePreference(
    preference: PreferenceEntry.StringPref,
    @StringRes title: Int,
    @StringRes description: Int,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val activity = context as? FragmentActivity

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri ->
            uri?.let {
                activity?.lifecycleScope?.launch {
                    preference.set(context, it.toString())
                }
            }
        }
    )

    PreferenceItem(
        title = stringResource(id = title),
        description = stringResource(id = description),
        onClick = {
            launcher.launch("image/*")
        },
        modifier = modifier
    )
}
