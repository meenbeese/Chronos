package com.meenbeese.chronos.data.preference

import android.content.Context

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity

import com.meenbeese.chronos.R
import com.meenbeese.chronos.fragments.AboutFragment
import com.meenbeese.chronos.views.PreferenceItem

/**
 * A preference item that opens the application's about screen.
 */
@Composable
fun AboutPreference(
    context: Context,
    modifier: Modifier = Modifier
) {
    PreferenceItem(
        title = stringResource(id = R.string.title_about),
        onClick = {
            if (context is FragmentActivity) {
                context.supportFragmentManager.beginTransaction()
                    .setCustomAnimations(
                        R.anim.fade_in,
                        R.anim.fade_out,
                        R.anim.fade_in,
                        R.anim.fade_out
                    )
                    .replace(R.id.fragment, AboutFragment())
                    .addToBackStack(null)
                    .commit()
            }
        },
        modifier = modifier.padding(bottom = 16.dp)
    )
}
