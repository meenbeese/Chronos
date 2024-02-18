package com.meenbeese.chronos.dialogs

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView

import com.afollestad.aesthetic.Aesthetic.Companion.get

import com.meenbeese.chronos.R

import java.util.concurrent.TimeUnit


class TimeChooserDialog(context: Context?) : AestheticDialog(context), View.OnClickListener {
    private var time: TextView? = null
    private var backspace: ImageView? = null
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
        setContentView(R.layout.dialog_time_chooser)
        time = findViewById(R.id.time)
        backspace = findViewById(R.id.backspace)
        time?.text = getTime()
        backspace?.setOnClickListener(this)

        findViewById<View>(R.id.one)?.setOnClickListener(this)
        findViewById<View>(R.id.two)?.setOnClickListener(this)
        findViewById<View>(R.id.three)?.setOnClickListener(this)
        findViewById<View>(R.id.four)?.setOnClickListener(this)
        findViewById<View>(R.id.five)?.setOnClickListener(this)
        findViewById<View>(R.id.six)?.setOnClickListener(this)
        findViewById<View>(R.id.seven)?.setOnClickListener(this)
        findViewById<View>(R.id.eight)?.setOnClickListener(this)
        findViewById<View>(R.id.nine)?.setOnClickListener(this)
        findViewById<View>(R.id.zero)?.setOnClickListener(this)

        val startButton = findViewById<TextView>(R.id.start)
        startButton?.setText(android.R.string.ok)
        startButton?.setOnClickListener {
            if (input.toInt() > 0) {
                listener?.onTimeChosen(
                    input.substring(0, 2).toInt(),
                    input.substring(2, 4).toInt(),
                    input.substring(4, 6).toInt()
                )
                dismiss()
            }
        }
        findViewById<View>(R.id.cancel)?.setOnClickListener { dismiss() }
        get()
            .textColorPrimary()
            .take(1)
            .subscribe { integer: Int? ->
                backspace?.setColorFilter(
                    integer!!
                )
            }
    }

    private fun input(character: String) {
        input = input.substring(character.length) + character
        time?.text = getTime()
    }

    private fun backspace() {
        input = "0" + input.substring(0, input.length - 1)
        time?.text = getTime()
    }

    private fun getTime(): String {
        val hours = input.substring(0, 2).toInt()
        val minutes = input.substring(2, 4).toInt()
        val seconds = input.substring(4, 6).toInt()

        backspace?.visibility = if (hours == 0 && minutes == 0 && seconds == 0) View.GONE else View.VISIBLE

        return if (hours > 0) {
            "%dh %02dm %02ds".format(hours, minutes, seconds)
        } else {
            "%dm %02ds".format(minutes, seconds)
        }
    }

    override fun onClick(view: View) {
        if (view is TextView) input(view.text.toString()) else backspace()
    }

    interface OnTimeChosenListener {
        fun onTimeChosen(hours: Int, minutes: Int, seconds: Int)
    }
}
