package com.meenbeese.chronos.nav

import kotlinx.serialization.Serializable

@Serializable
object HomeRoute

@Serializable
object WatchRoute

@Serializable
object AboutRoute

@Serializable
data class TimerRoute(
    val timerId: Int
)
