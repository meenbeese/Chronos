package com.meenbeese.chronos.db

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope

import com.meenbeese.chronos.data.AlarmData
import com.meenbeese.chronos.data.toData

import kotlinx.coroutines.launch

class AlarmViewModel(private val repository: AlarmRepository) : ViewModel() {
    val alarms: LiveData<List<AlarmData>> = repository
        .getAll()
        .map { entities ->
            entities.map { it.toData() }
        }

    fun insert(alarm: AlarmEntity) = viewModelScope.launch {
        repository.insert(alarm)
    }

    suspend fun insertAndReturnId(alarm: AlarmEntity): Long {
        return repository.insert(alarm)
    }

    fun update(alarm: AlarmEntity) = viewModelScope.launch {
        repository.update(alarm)
    }

    fun delete(alarm: AlarmEntity) = viewModelScope.launch {
        repository.delete(alarm)
    }
}
