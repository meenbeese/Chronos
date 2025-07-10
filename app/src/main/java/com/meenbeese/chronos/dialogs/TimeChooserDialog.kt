package com.meenbeese.chronos.dialogs

import android.content.Context
import android.os.Bundle

import androidx.activity.ComponentDialog

import com.meenbeese.chronos.databinding.DialogTimePickerBinding

class TimeChooserDialog(
    context: Context,
    private val initialHour: Int = 0,
    private val initialMinute: Int = 0,
    private val is24HourClock: Boolean = true,
    private val onTimeSet: (hour: Int, minute: Int) -> Unit
) : ComponentDialog(context) {

    private var _binding: DialogTimePickerBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = DialogTimePickerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.timePicker.apply {
            setIs24HourView(is24HourClock)
            hour = initialHour
            minute = initialMinute
        }

        binding.cancelButton.setOnClickListener {
            dismiss()
        }

        binding.okButton.setOnClickListener {
            val selectedHour = binding.timePicker.hour
            val selectedMinute = binding.timePicker.minute
            onTimeSet(selectedHour, selectedMinute)
            dismiss()
        }
    }

    override fun onStop() {
        super.onStop()
        _binding = null
    }
}
