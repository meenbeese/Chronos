package com.meenbeese.chronos.db

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope

import kotlinx.coroutines.launch


class AlarmViewModel(private val repository: AlarmRepository) : ViewModel() {
    fun insert(alarm: AlarmEntity) = viewModelScope.launch {
        repository.insert(alarm)
    }

    fun update(alarm: AlarmEntity) = viewModelScope.launch {
        repository.update(alarm)
    }

    fun delete(alarm: AlarmEntity) = viewModelScope.launch {
        repository.delete(alarm)
    }
}
