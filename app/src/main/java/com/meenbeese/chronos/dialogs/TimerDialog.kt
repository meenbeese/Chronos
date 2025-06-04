package com.meenbeese.chronos.dialogs

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.content.Context
import android.os.Bundle
import android.view.HapticFeedbackConstants
import android.view.View

import androidx.activity.ComponentDialog
import androidx.fragment.app.FragmentManager
import androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat

import com.google.android.material.textview.MaterialTextView
import com.meenbeese.chronos.Chronos
import com.meenbeese.chronos.R
import com.meenbeese.chronos.data.PreferenceData
import com.meenbeese.chronos.data.SoundData
import com.meenbeese.chronos.databinding.DialogTimerBinding
import com.meenbeese.chronos.fragments.TimerFragment
import com.meenbeese.chronos.interfaces.SoundChooserListener
import com.meenbeese.chronos.services.TimerService
import com.meenbeese.chronos.utils.Option

import java.util.concurrent.TimeUnit

class TimerDialog(
    context: Context,
    private val manager: FragmentManager
) : ComponentDialog(context), View.OnClickListener {

    private var _binding: DialogTimerBinding? = null
    private val binding get() = _binding!!
    private val chronos: Chronos = context.applicationContext as Chronos

    private var ringtone: SoundData? = PreferenceData.DEFAULT_TIMER_RINGTONE.getValue<String>(context)?.let {
        when (val opt = SoundData.fromString(it)) {
            is Option.Some -> opt.value
            is Option.None -> null
        }
    }

    private var isVibrate = true
    private var input = "000000"

    @SuppressLint("CheckResult")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = DialogTimerBinding.inflate(layoutInflater)
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

        updateRingtoneUI()

        binding.ringtone.setOnClickListener {
            val dialog = SoundChooserDialog()
            dialog.setListener(object : SoundChooserListener {
                override fun onSoundChosen(sound: SoundData?) {
                    ringtone = sound
                    updateRingtoneUI()
                }
            })
            dialog.show(manager, "")
        }

        binding.vibrate.setOnClickListener {
            isVibrate = !isVibrate
            val drawable = AnimatedVectorDrawableCompat.create(
                it.context,
                if (isVibrate) R.drawable.ic_none_to_vibrate else R.drawable.ic_vibrate_to_none
            )
            if (drawable != null) {
                binding.vibrateImage.setImageDrawable(drawable)
                drawable.start()
            } else binding.vibrateImage.setImageResource(if (isVibrate) R.drawable.ic_vibrate else R.drawable.ic_none)
            binding.vibrateImage.animate().alpha(if (isVibrate) 1f else 0.333f).start()
            if (isVibrate) it.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
        }

        binding.start.setOnClickListener { view: View ->
            if (input.toInt() > 0) {
                val timer = chronos.newTimer()
                timer.setDuration(millis, chronos)
                timer.setVibrate(view.context, isVibrate)
                timer.setSound(view.context, ringtone)
                timer[chronos] = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
                TimerService.startService(context)
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

        binding.cancel.setOnClickListener { dismiss() }
    }

    private fun updateRingtoneUI() {
        binding.ringtoneImage.setImageResource(if (ringtone != null) R.drawable.ic_ringtone else R.drawable.ic_ringtone_disabled)
        binding.ringtoneImage.alpha = if (ringtone != null) 1f else 0.333f
        binding.ringtoneText.text = ringtone?.name ?: context.getString(R.string.title_sound_none)
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
        when (view) {
            binding.backspace -> backspace()
            is MaterialTextView -> input(view.text.toString())
        }
    }
}
