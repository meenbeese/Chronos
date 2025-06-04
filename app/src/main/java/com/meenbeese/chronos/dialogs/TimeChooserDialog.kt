package com.meenbeese.chronos.dialogs

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.View

import androidx.activity.ComponentDialog

import com.google.android.material.textview.MaterialTextView
import com.meenbeese.chronos.databinding.DialogTimeChooserBinding

import java.util.concurrent.TimeUnit

class TimeChooserDialog(context: Context?) : ComponentDialog(context!!), View.OnClickListener {

    private var _binding: DialogTimeChooserBinding? = null
    private val binding get() = _binding!!

    private var input = "000000"
    private var listener: OnTimeChosenListener? = null

    fun setDefault(inputHours: Int, inputMinutes: Int, inputSeconds: Int) {
        var hours = inputHours
        var minutes = inputMinutes
        var seconds = inputSeconds

        hours += TimeUnit.MINUTES.toHours(minutes.toLong()).toInt()
        minutes = (minutes % TimeUnit.HOURS.toMinutes(1) + TimeUnit.SECONDS.toMinutes(seconds.toLong())).toInt()
        seconds %= TimeUnit.MINUTES.toSeconds(1).toInt()

        input = "%02d%02d%02d".format(hours, minutes, seconds)
    }

    fun setListener(listener: OnTimeChosenListener?) {
        this.listener = listener
    }

    @SuppressLint("CheckResult")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = DialogTimeChooserBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.time.text = getTime()
        binding.backspace.setOnClickListener(this)

        binding.one.setOnClickListener(this)
        binding.two.setOnClickListener(this)
        binding.three.setOnClickListener(this)
        binding.four.setOnClickListener(this)
        binding.five.setOnClickListener(this)
        binding.six.setOnClickListener(this)
        binding.seven.setOnClickListener(this)
        binding.eight.setOnClickListener(this)
        binding.nine.setOnClickListener(this)
        binding.zero.setOnClickListener(this)

        binding.start.setText(android.R.string.ok)
        binding.start.setOnClickListener {
            if (input.toInt() > 0) {
                listener?.onTimeChosen(
                    input.substring(0, 2).toInt(),
                    input.substring(2, 4).toInt(),
                    input.substring(4, 6).toInt()
                )
                dismiss()
            }
        }

        binding.cancel.setOnClickListener { dismiss() }
    }

    private fun input(character: String) {
        input = input.substring(character.length) + character
        binding.time.text = getTime()
    }

    private fun backspace() {
        input = "0" + input.substring(0, input.length - 1)
        binding.time.text = getTime()
    }

    private fun getTime(): String {
        val hours = input.substring(0, 2).toInt()
        val minutes = input.substring(2, 4).toInt()
        val seconds = input.substring(4, 6).toInt()

        binding.backspace.visibility = if (hours == 0 && minutes == 0 && seconds == 0) View.GONE else View.VISIBLE

        return if (hours > 0) {
            "%dh %02dm %02ds".format(hours, minutes, seconds)
        } else {
            "%dm %02ds".format(minutes, seconds)
        }
    }

    override fun onClick(view: View) {
        when (view) {
            binding.backspace -> backspace()
            is MaterialTextView -> input(view.text.toString())
        }
    }

    interface OnTimeChosenListener {
        fun onTimeChosen(hours: Int, minutes: Int, seconds: Int)
    }
}
