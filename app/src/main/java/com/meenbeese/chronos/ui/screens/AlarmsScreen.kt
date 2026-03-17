package com.meenbeese.chronos.ui.screens

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext

import com.meenbeese.chronos.data.AlarmData
import com.meenbeese.chronos.data.TimerData
import com.meenbeese.chronos.ui.views.AlarmListView
import com.meenbeese.chronos.ui.views.EmptyAlarmsView
import com.meenbeese.chronos.ui.views.ProgressLineView
import com.meenbeese.chronos.ui.views.TimerItemView

import kotlinx.coroutines.delay

@Composable
fun AlarmsScreen(
    alarms: List<AlarmData>,
    timers: List<TimerData>,
    scrollToAlarmId: Int?,
    onScrollHandled: () -> Unit,
    onAlarmUpdated: (AlarmData) -> Unit,
    onAlarmDeleted: (AlarmData) -> Unit,
    isBottomSheetExpanded: MutableState<Boolean> = remember { mutableStateOf(false) }
) {
    val context = LocalContext.current

    if (alarms.isEmpty() && timers.isEmpty()) {
        EmptyAlarmsView()
    } else {
        val listState = rememberLazyListState()
        val activeTimers = timers.filter { it.isSet }
        var expandAlarmId by remember { mutableStateOf<Int?>(null) }
        var highlightAlarmId by remember { mutableStateOf<Int?>(null) }

        LaunchedEffect(scrollToAlarmId) {
            val targetId = scrollToAlarmId ?: return@LaunchedEffect
            expandAlarmId = targetId
            highlightAlarmId = targetId
        }

        LaunchedEffect(highlightAlarmId) {
            if (highlightAlarmId == null) return@LaunchedEffect
            delay(2500)
            highlightAlarmId = null
        }

        LaunchedEffect(scrollToAlarmId, alarms, activeTimers.size) {
            val targetId = scrollToAlarmId ?: return@LaunchedEffect
            val index = alarms.indexOfFirst { it.id == targetId }

            if (index != -1) {
                listState.animateScrollToItem(index + activeTimers.size)
            }

            onScrollHandled()
        }

        val isScrollingDown = listState.isScrollingDown()

        LaunchedEffect(isScrollingDown) {
            if (isScrollingDown && !isBottomSheetExpanded.value) {
                isBottomSheetExpanded.value = true
            } else if (!isScrollingDown && isBottomSheetExpanded.value) {
                isBottomSheetExpanded.value = false
            }
        }

        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize()
        ) {
            items(
                items = activeTimers,
                key = { it.id }
            ) { timer ->
                TimerItemView(
                    timer = timer,
                    onStopClick = { timer.onRemoved(context) },
                    modifier = Modifier.fillMaxWidth()
                ) { progress ->
                    ProgressLineView(progress = progress)
                }
            }

            items(
                items = alarms,
                key = { it.id }
            ) { alarm ->
                AlarmListView(
                    alarm = alarm,
                    onAlarmUpdated = onAlarmUpdated,
                    onAlarmDeleted = onAlarmDeleted,
                    forceExpanded = alarm.id == expandAlarmId,
                    highlighted = alarm.id == highlightAlarmId,
                    onForceExpandHandled = {
                        if (expandAlarmId == alarm.id) {
                            expandAlarmId = null
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun LazyListState.isScrollingDown(): Boolean {
    var previousIndex by remember { mutableStateOf(firstVisibleItemIndex) }
    var previousOffset by remember { mutableStateOf(firstVisibleItemScrollOffset) }

    return remember(this) {
        derivedStateOf {
            val scrollingDown = firstVisibleItemIndex > previousIndex ||
                (firstVisibleItemIndex == previousIndex &&
                firstVisibleItemScrollOffset > previousOffset)

            previousIndex = firstVisibleItemIndex
            previousOffset = firstVisibleItemScrollOffset

            scrollingDown
        }
    }.value
}
