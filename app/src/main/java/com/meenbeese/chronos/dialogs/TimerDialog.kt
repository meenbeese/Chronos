package com.meenbeese.chronos.dialogs

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.content.Context
import android.os.Bundle
import android.view.HapticFeedbackConstants
import android.view.View
import android.widget.ImageView
import android.widget.TextView

import androidx.fragment.app.FragmentManager
import androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat

import com.meenbeese.chronos.Chronos
import com.meenbeese.chronos.R
import com.meenbeese.chronos.data.PreferenceData
import com.meenbeese.chronos.data.SoundData
import com.meenbeese.chronos.fragments.TimerFragment
import com.meenbeese.chronos.interfaces.SoundChooserListener
import com.afollestad.aesthetic.Aesthetic.Companion.get

import java.util.concurrent.TimeUnit


class TimerDialog(context: Context, private val manager: FragmentManager) :
    AestheticDialog(context), View.OnClickListener {
    private var ringtoneImage: ImageView? = null
    private var ringtoneText: TextView? = null
    private var vibrateImage: ImageView? = null
    private var ringtone: SoundData?
    private var isVibrate = true
    private var time: TextView? = null
    private var backspace: ImageView? = null
    private var input = "000000"
    private val chronos: Chronos

    init {
        chronos = context.applicationContext as Chronos
        ringtone = SoundData.fromString(PreferenceData.DEFAULT_TIMER_RINGTONE.getValue(context, ""))
    }

    @SuppressLint("CheckResult")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_timer)
        ringtoneImage = findViewById(R.id.ringtoneImage)
        ringtoneText = findViewById(R.id.ringtoneText)
        vibrateImage = findViewById(R.id.vibrateImage)
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

        ringtoneImage?.setImageResource(if (ringtone != null) R.drawable.ic_ringtone else R.drawable.ic_ringtone_disabled)
        ringtoneImage?.alpha = if (ringtone != null) 1f else 0.333f
        if (ringtone != null) ringtoneText?.text = ringtone?.name else ringtoneText?.setText(R.string.title_sound_none)

        findViewById<View>(R.id.ringtone)?.setOnClickListener {
            val dialog = SoundChooserDialog()
            dialog.setListener(object : SoundChooserListener {
                override fun onSoundChosen(sound: SoundData?) {
                    ringtone = sound
                    ringtoneImage?.setImageResource(if (sound != null) R.drawable.ic_ringtone else R.drawable.ic_ringtone_disabled)
                    ringtoneImage?.alpha = if (sound != null) 1f else 0.333f
                    if (sound != null) ringtoneText?.text = sound.name else ringtoneText?.setText(R.string.title_sound_none)
                }
            })
            dialog.show(manager, "")
        }
        findViewById<View>(R.id.vibrate)!!.setOnClickListener { v: View ->
            isVibrate = !isVibrate
            val drawable = AnimatedVectorDrawableCompat.create(
                v.context,
                if (isVibrate) R.drawable.ic_none_to_vibrate else R.drawable.ic_vibrate_to_none
            )
            if (drawable != null) {
                vibrateImage?.setImageDrawable(drawable)
                drawable.start()
            } else vibrateImage?.setImageResource(if (isVibrate) R.drawable.ic_vibrate else R.drawable.ic_none)
            vibrateImage?.animate()?.alpha(if (isVibrate) 1f else 0.333f)?.start()
            if (isVibrate) v.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
        }
        findViewById<View>(R.id.start)?.setOnClickListener { view: View ->
            if (input.toInt() > 0) {
                val timer = chronos.newTimer()
                timer.setDuration(millis, chronos)
                timer.setVibrate(view.context, isVibrate)
                timer.setSound(view.context, ringtone)
                timer[chronos] = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
                chronos.onTimerStarted()
                val args = Bundle()
                args.putParcelable(TimerFragment.EXTRA_TIMER, timer)
                val fragment = TimerFragment()
                fragment.arguments = args
                manager.beginTransaction()
                    .setCustomAnimations(
                        R.anim.slide_in_up_sheet,
                        R.anim.slide_out_up_sheet,
                        R.anim.slide_in_down_sheet,
                        R.anim.slide_out_down_sheet
                    )
                    .replace(R.id.fragment, fragment)
                    .addToBackStack(null)
                    .commit()
                dismiss()
            }
        }
        findViewById<View>(R.id.cancel)?.setOnClickListener { dismiss() }
        get()
            .textColorPrimary()
            .take(1)
            .subscribe { integer: Int ->
                ringtoneImage?.setColorFilter(integer)
                vibrateImage?.setColorFilter(integer)
                backspace?.setColorFilter(integer)
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

    private val millis: Long
        get() {
            var millis: Long = 0
            val hours = input.substring(0, 2).toInt()
            val minutes = input.substring(2, 4).toInt()
            val seconds = input.substring(4, 6).toInt()
            millis += TimeUnit.HOURS.toMillis(hours.toLong())
            millis += TimeUnit.MINUTES.toMillis(minutes.toLong())
            millis += TimeUnit.SECONDS.toMillis(seconds.toLong())
            return millis
        }

    override fun onClick(view: View) {
        if (view is TextView) input(view.text.toString()) else backspace()
    }
}
