package com.meenbeese.chronos.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

import com.meenbeese.chronos.db.TimerAlarmRepository

class HomeViewModelFactory(
    private val timerRepo: TimerAlarmRepository,
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return HomeViewModel(timerRepo) as T
    }
}
