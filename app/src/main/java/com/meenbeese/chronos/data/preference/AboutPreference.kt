package com.meenbeese.chronos.data.preference

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

import com.meenbeese.chronos.R
import com.meenbeese.chronos.nav.NavScreen
import com.meenbeese.chronos.ui.views.PreferenceItem

/**
 * A preference item that opens the application's about screen.
 */
@Composable
fun AboutPreference(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    PreferenceItem(
        title = stringResource(id = R.string.title_about),
        onClick = {
            navController.navigate(NavScreen.About.route)
        },
        modifier = modifier.padding(bottom = 16.dp)
    )
}
