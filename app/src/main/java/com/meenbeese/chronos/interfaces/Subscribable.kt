package com.meenbeese.chronos.interfaces

/**
 * A basic interface to allow views, dialogs, and activities
 * and stuff to be subscribed and un-subscribed easily
 * to adhere to their respective life-cycles.
 */
interface Subscribable {
    fun subscribe()
    fun unsubscribe()
}
