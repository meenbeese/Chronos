package com.meenbeese.chronos.ui.screens

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

import com.meenbeese.chronos.adapters.AlarmsAdapter
import com.meenbeese.chronos.data.toData
import com.meenbeese.chronos.db.AlarmEntity
import com.meenbeese.chronos.ui.views.EmptyAlarmsView

@Composable
fun AlarmsScreen(
    alarms: List<AlarmEntity>,
    adapter: AlarmsAdapter,
    onScrolledToEnd: () -> Unit,
    isBottomSheetExpanded: MutableState<Boolean> = remember { mutableStateOf(false) }
) {
    if (adapter.itemCount == 0) {
        EmptyAlarmsView()
    } else {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                RecyclerView(ctx).apply {
                    layoutManager = LinearLayoutManager(ctx)
                    this.adapter = adapter

                    addOnScrollListener(object : RecyclerView.OnScrollListener() {
                        private var lastOffset = 0
                        private var lastChangeTime = 0L
                        private val debounceInterval = 300L

                        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                            val offset = recyclerView.computeVerticalScrollOffset()
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

                            // Detect scrolled to bottom
                            val layoutManager = recyclerView.layoutManager as? LinearLayoutManager ?: return
                            val lastVisibleItem = layoutManager.findLastCompletelyVisibleItemPosition()
                            val totalItemCount = layoutManager.itemCount

                            if (lastVisibleItem == totalItemCount - 1 && totalItemCount > 0) {
                                onScrolledToEnd()
                            }
                        }
                    })
                }
            },
            update = { recyclerView ->
                adapter.updateAlarms(alarms.map { it.toData() })
                recyclerView.adapter = adapter
            }
        )
    }
}
