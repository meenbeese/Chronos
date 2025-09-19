package com.meenbeese.chronos.nav

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable

import com.meenbeese.chronos.data.TimerData
import com.meenbeese.chronos.nav.destinations.AboutDestination
import com.meenbeese.chronos.nav.destinations.HomeDestination
import com.meenbeese.chronos.nav.destinations.StopwatchDestination
import com.meenbeese.chronos.nav.destinations.TimerDestination

@Composable
fun NavGraph(navController: NavHostController) {
    val context = LocalContext.current

    NavHost(navController = navController, startDestination = NavScreen.Home.route) {
        composable(NavScreen.Home.route) {
            HomeDestination(
                navController = navController,
                context = context,
                intentAction = null,
                navigateToStopwatch = { navController.navigate(NavScreen.Watch.route) },
                navigateToTimer = { timer ->
                    navController.currentBackStackEntry?.savedStateHandle?.set("timer", timer)
                    navController.navigate(NavScreen.Timer.route)
                },
            )
        }

        composable(NavScreen.Watch.route) {
            StopwatchDestination(navController)
        }

        composable(NavScreen.Timer.route) { backStackEntry ->
            val timer: TimerData? = navController.previousBackStackEntry
                ?.savedStateHandle
                ?.get<TimerData>("timer")

            if (timer != null) {
                TimerDestination(navController, timer)
            }
        }

        composable(NavScreen.About.route) {
            AboutDestination(context)
        }
    }
}
