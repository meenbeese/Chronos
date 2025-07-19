package com.meenbeese.chronos.nav

sealed class NavScreen(val route: String) {
    object Home : NavScreen("home")
    object Timer : NavScreen("timer")
    object Watch : NavScreen("watch")
    object About : NavScreen("about")
}
