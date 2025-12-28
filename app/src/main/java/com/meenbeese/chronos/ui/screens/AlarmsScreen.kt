package com.meenbeese.chronos.ui.screens

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier

import com.meenbeese.chronos.data.AlarmData
import com.meenbeese.chronos.data.toData
import com.meenbeese.chronos.db.AlarmEntity
import com.meenbeese.chronos.ui.views.EmptyAlarmsView

import kotlinx.coroutines.flow.collectLatest

@Composable
fun AlarmsScreen(
    alarms: List<AlarmData>,
    scrollToAlarmId: Int?,
    onScrollHandled: () -> Unit,
    onAlarmUpdated: (AlarmData) -> Unit,
    onAlarmDeleted: (AlarmData) -> Unit,
    isBottomSheetExpanded: MutableState<Boolean> = remember { mutableStateOf(false) }
) {
    if (alarms.isEmpty()) {
        EmptyAlarmsView()
    } else {
        val listState = rememberLazyListState()

        LaunchedEffect(scrollToAlarmId, alarms) {
            val targetId = scrollToAlarmId ?: return@LaunchedEffect

            val index = alarms.indexOfFirst { it.id == targetId }
            if (index >= 0) {
                listState.animateScrollToItem(index)
            }

            onScrollHandled()
        }

        LaunchedEffect(listState) {
            var lastOffset = 0
            var lastChangeTime = 0L
            val debounceInterval = 300L

            snapshotFlow { listState.firstVisibleItemScrollOffset }
                .collectLatest { offset ->
                    val now = System.currentTimeMillis()
                    val direction = offset - lastOffset

                    if (direction > 10 && !isBottomSheetExpanded.value) {
                        if (now - lastChangeTime > debounceInterval) {
                            isBottomSheetExpanded.value = true
                            lastChangeTime = now
                        }
                    } else if (direction < -10 && isBottomSheetExpanded.value) {
                        if (now - lastChangeTime > debounceInterval) {
                            isBottomSheetExpanded.value = false
                            lastChangeTime = now
                        }
                    }

                    lastOffset = offset
                }
        }

        AlarmListScreen(
            alarms = alarms,
            onAlarmUpdated = onAlarmUpdated,
            onAlarmDeleted = onAlarmDeleted,
            modifier = Modifier.fillMaxSize(),
            listState = listState
        )
    }
}
