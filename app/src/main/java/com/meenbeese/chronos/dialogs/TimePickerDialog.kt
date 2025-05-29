package com.meenbeese.chronos.dialogs

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.TimePicker

import androidx.activity.ComponentDialog

import com.google.android.material.button.MaterialButton
import com.meenbeese.chronos.R

class TimePickerDialog(
    context: Context,
    private val initialHour: Int = 0,
    private val initialMinute: Int = 0,
    private val is24HourClock: Boolean = true,
    private val onTimeSet: (hour: Int, minute: Int) -> Unit
) : ComponentDialog(context) {

    private var timePicker: TimePicker? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_time_picker, null)
        setContentView(view)

        timePicker = view.findViewById(R.id.timePicker)
        timePicker?.apply {
            setIs24HourView(is24HourClock)
            hour = initialHour
            minute = initialMinute
        }

        view.findViewById<MaterialButton>(R.id.cancelButton).setOnClickListener {
            dismiss()
        }

        view.findViewById<MaterialButton>(R.id.okButton).setOnClickListener {
            val selectedHour = timePicker?.hour ?: 0
            val selectedMinute = timePicker?.minute ?: 0
            onTimeSet(selectedHour, selectedMinute)
            dismiss()
        }
    }
}
