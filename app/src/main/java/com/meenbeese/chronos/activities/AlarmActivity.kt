package com.meenbeese.chronos.activities

import android.app.AlarmManager
import android.content.Intent
import android.media.AudioManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.HapticFeedbackConstants
import android.view.WindowInsetsController
import android.view.WindowManager

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.media3.common.util.UnstableApi

import com.meenbeese.chronos.Chronos
import com.meenbeese.chronos.R
import com.meenbeese.chronos.data.AlarmData
import com.meenbeese.chronos.data.PreferenceData
import com.meenbeese.chronos.data.SoundData
import com.meenbeese.chronos.data.TimerData
import com.meenbeese.chronos.databinding.ActivityAlarmBinding
import com.meenbeese.chronos.dialogs.SnoozeDurationDialog
import com.meenbeese.chronos.dialogs.TimeChooserDialog
import com.meenbeese.chronos.dialogs.TimeChooserDialog.OnTimeChosenListener
import com.meenbeese.chronos.services.SleepReminderService.Companion.refreshSleepTime
import com.meenbeese.chronos.services.TimerService
import com.meenbeese.chronos.utils.FormatUtils
import com.meenbeese.chronos.utils.FormatUtils.format
import com.meenbeese.chronos.utils.FormatUtils.formatMillis
import com.meenbeese.chronos.utils.FormatUtils.formatUnit
import com.meenbeese.chronos.utils.FormatUtils.getShortFormat
import com.meenbeese.chronos.utils.ImageUtils.getBackgroundImage
import com.meenbeese.chronos.views.SlideActionView

import java.util.Date
import java.util.concurrent.TimeUnit

import kotlin.math.min

class AlarmActivity : ComponentActivity() {
    private lateinit var binding: ActivityAlarmBinding
    private var chronos: Chronos? = null
    private var vibrator: Vibrator? = null
    private var audioManager: AudioManager? = null

    private var triggerMillis: Long = 0
    private var alarm: AlarmData? = null
    private var sound: SoundData? = null
    private var isVibrate = false
    private var isSlowWake = false
    private var slowWakeMillis: Long = 0
    private var currentVolume = 0
    private var minVolume = 0
    private var originalVolume = 0
    private var volumeRange = 0
    private var handler: Handler? = null
    private var runnable: Runnable? = null

    @UnstableApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAlarmBinding.inflate(layoutInflater)
        setContentView(binding.root)

        WindowCompat.setDecorFitsSystemWindows(window, false)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        window.insetsController?.apply {
            hide(WindowInsetsCompat.Type.systemBars())
            systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }

        chronos = applicationContext as Chronos

        binding.slideView.setContent {
            SlideActionView(
                modifier = Modifier.fillMaxSize(),
                handleColor = Color.Gray,
                outlineColor = Color.Gray,
                iconColor = Color.Black,
                leftIcon = painterResource(R.drawable.ic_snooze),
                rightIcon = painterResource(R.drawable.ic_close),
                onSlideLeft = { onSlideLeft() },
                onSlideRight = { onSlideRight() }
            )
        }

        isSlowWake = PreferenceData.SLOW_WAKE_UP.getValue(this)
        slowWakeMillis = PreferenceData.SLOW_WAKE_UP_TIME.getValue(this)

        when {
            intent.hasExtra(EXTRA_ALARM) -> {
                alarm = intent.getParcelableExtra(EXTRA_ALARM)
                isVibrate = alarm?.isVibrate == true
                sound = alarm?.sound
            }
            intent.hasExtra(EXTRA_TIMER) -> {
                val timer = intent.getParcelableExtra<TimerData>(EXTRA_TIMER)
                isVibrate = timer?.isVibrate == true
                sound = timer?.sound
            }
            else -> finish()
        }

        binding.date.text = format(Date(), FormatUtils.FORMAT_DATE + ", " + getShortFormat(this))

        sound?.let {
            if (!it.isSetVolumeSupported) {
                audioManager = getSystemService(AUDIO_SERVICE) as AudioManager
                originalVolume = audioManager!!.getStreamVolume(AudioManager.STREAM_ALARM)
                if (isSlowWake) {
                    minVolume = 0
                    volumeRange = originalVolume - minVolume
                    currentVolume = minVolume
                    audioManager?.setStreamVolume(AudioManager.STREAM_ALARM, minVolume, 0)
                }
            }
        }

        vibrator = getSystemService(VIBRATOR_SERVICE) as Vibrator
        triggerMillis = System.currentTimeMillis()
        handler = Handler(Looper.getMainLooper())
        runnable = object : Runnable {
            override fun run() {
                val elapsedMillis = System.currentTimeMillis() - triggerMillis
                binding.time.text = "-${formatMillis(elapsedMillis).dropLast(3)}"

                if (isVibrate) {
                    vibrator!!.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE))
                }

                sound?.let {
                    if (!it.isPlaying(chronos!!)) it.play(chronos!!)
                    if (alarm != null && isSlowWake) {
                        val slowWakeProgress = elapsedMillis.toFloat() / slowWakeMillis
                        window.addFlags(WindowManager.LayoutParams.FLAGS_CHANGED)
                        if (it.isSetVolumeSupported) {
                            it.setVolume(chronos!!, min(1f, slowWakeProgress))
                        } else if (currentVolume < originalVolume) {
                            val newVolume = minVolume + min(originalVolume.toFloat(), slowWakeProgress * volumeRange).toInt()
                            if (newVolume != currentVolume) {
                                audioManager?.setStreamVolume(AudioManager.STREAM_ALARM, newVolume, 0)
                                currentVolume = newVolume
                            }
                        }
                    }
                }

                handler?.postDelayed(this, 1000)
            }
        }

        handler?.post(runnable!!)
        sound?.play(chronos!!)
        refreshSleepTime(chronos!!)

        if (PreferenceData.RINGING_BACKGROUND_IMAGE.getValue(this)) {
            getBackgroundImage(binding.background)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        stopAnnoyance()
    }

    private fun stopAnnoyance() {
        handler?.removeCallbacks(runnable!!)
        if (sound?.isPlaying(chronos!!) == true) {
            sound?.stop(chronos!!)
            if (isSlowWake && sound?.isSetVolumeSupported == false) {
                audioManager?.setStreamVolume(AudioManager.STREAM_ALARM, originalVolume, 0)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        finish()
        startActivity(Intent(intent))
    }

    fun onSlideLeft() {
        val minutes = intArrayOf(2, 5, 10, 20, 30, 60)
        val names = Array<CharSequence?>(minutes.size + 1) { i ->
            if (i < minutes.size) formatUnit(this@AlarmActivity, minutes[i]) else getString(R.string.title_snooze_custom)
        }

        stopAnnoyance()

        val snoozeDurationDialog = SnoozeDurationDialog(this, names, object : SnoozeDurationDialog.OnSnoozeDurationSelectedListener {
            override fun onSnoozeDurationSelected(which: Int) {
                if (which < minutes.size) {
                    chronos!!.newTimer().apply {
                        setDuration(TimeUnit.MINUTES.toMillis(minutes[which].toLong()), chronos!!)
                        setVibrate(this@AlarmActivity, isVibrate)
                        setSound(this@AlarmActivity, sound)
                        this[chronos!!] = getSystemService(ALARM_SERVICE) as AlarmManager
                    }
                    TimerService.startService(baseContext)
                    finish()
                } else {
                    TimeChooserDialog(this@AlarmActivity).apply {
                        setListener(object : OnTimeChosenListener {
                            override fun onTimeChosen(hours: Int, minutes: Int, seconds: Int) {
                                chronos?.newTimer()?.apply {
                                    setVibrate(this@AlarmActivity, isVibrate)
                                    setSound(this@AlarmActivity, sound)
                                    setDuration(
                                        TimeUnit.HOURS.toMillis(hours.toLong()) +
                                                TimeUnit.MINUTES.toMillis(minutes.toLong()) +
                                                TimeUnit.SECONDS.toMillis(seconds.toLong()),
                                        chronos!!
                                    )
                                    this[chronos!!] = getSystemService(ALARM_SERVICE) as AlarmManager
                                }
                                TimerService.startService(baseContext)
                                finish()
                            }
                        })
                        show()
                    }
                }
            }
        })
        snoozeDurationDialog.show()

        binding.overlay.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
    }

    fun onSlideRight() {
        binding.overlay.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
        finish()
    }

    companion object {
        const val EXTRA_ALARM = "meenbeese.chronos.AlarmActivity.EXTRA_ALARM"
        const val EXTRA_TIMER = "meenbeese.chronos.AlarmActivity.EXTRA_TIMER"
    }
}
