package com.meenbeese.chronos.nav

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute

import com.meenbeese.chronos.nav.destinations.AboutDestination
import com.meenbeese.chronos.nav.destinations.HomeDestination
import com.meenbeese.chronos.nav.destinations.StopwatchDestination
import com.meenbeese.chronos.nav.destinations.TimerDestination

@Composable
fun NavGraph(navController: NavHostController) {
    val context = LocalContext.current

    NavHost(
        navController = navController,
        startDestination = HomeRoute
    ) {
        composable<HomeRoute> {
            HomeDestination(
                navController = navController,
                context = context,
                intentAction = null,
                navigateToStopwatch = {
                    navController.navigate(WatchRoute)
                },
                navigateToTimer = { timer ->
                    navController.navigate(TimerRoute(timer.id))
                },
            )
        }

        composable<WatchRoute> {
            StopwatchDestination(navController)
        }

        composable<TimerRoute> { entry ->
            val route = entry.toRoute<TimerRoute>()
            TimerDestination(navController, route.timerId)
        }

        composable<AboutRoute> {
            AboutDestination(context)
        }
    }
}
