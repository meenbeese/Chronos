package com.meenbeese.chronos.dialogs

import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.WindowManager

import androidx.activity.ComponentDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

import com.meenbeese.chronos.R
import com.meenbeese.chronos.adapters.TimeZonesAdapter

import java.util.TimeZone


class TimeZoneChooserDialog(context: Context?) : ComponentDialog(context!!) {
    private val excludedIds = arrayOfNulls<String>(0)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_time_zone_chooser)
        val recycler = findViewById<RecyclerView>(R.id.recycler)
        recycler?.layoutManager = LinearLayoutManager(context)
        val timeZones: MutableList<String> = ArrayList()
        for (id1 in TimeZone.getAvailableIDs()) {
            var isFine = true
            for (id2 in timeZones) {
                if (TimeZone.getTimeZone(id1).rawOffset == TimeZone.getTimeZone(id2).rawOffset) {
                    isFine = false
                    break
                }
            }
            for (id2 in excludedIds) {
                if (TimeZone.getTimeZone(id1).rawOffset == TimeZone.getTimeZone(id2).rawOffset) {
                    isFine = false
                    break
                }
            }
            if (isFine) timeZones.add(id1)
        }
        timeZones.sortWith(Comparator.comparingInt { id: String? -> TimeZone.getTimeZone(id).rawOffset })
        recycler?.adapter = TimeZonesAdapter(timeZones)
        findViewById<View>(R.id.ok)?.setOnClickListener { dismiss() }
    }

    override fun show() {
        super.show()
        val window = window
        window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
    }
}
