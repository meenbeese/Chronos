package com.meenbeese.chronos.dialogs

import android.content.Context
import android.os.Bundle
import android.view.WindowManager

import androidx.activity.ComponentDialog
import androidx.recyclerview.widget.LinearLayoutManager

import com.meenbeese.chronos.adapters.TimeZonesAdapter
import com.meenbeese.chronos.databinding.DialogTimeZoneChooserBinding

import java.util.TimeZone

class TimeZoneChooserDialog(
    context: Context,
    private val selectedTimeZones: MutableSet<String>,
    private val onSelectionDone: (Set<String>) -> Unit
) : ComponentDialog(context) {

    private val excludedIds = arrayOfNulls<String>(0)

    private var _binding: DialogTimeZoneChooserBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = DialogTimeZoneChooserBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.recycler.layoutManager = LinearLayoutManager(context)

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

        timeZones.sortWith(Comparator.comparingInt { id -> TimeZone.getTimeZone(id).rawOffset })

        binding.recycler.adapter = TimeZonesAdapter(timeZones, selectedTimeZones)

        binding.ok.setOnClickListener {
            onSelectionDone(selectedTimeZones)
            dismiss()
        }
    }

    override fun show() {
        super.show()
        val window = window
        window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
    }

    override fun onStop() {
        super.onStop()
        _binding = null
    }
}
