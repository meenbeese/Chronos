package com.meenbeese.chronos.activities

import android.app.AlarmManager
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.ActivityInfo
import android.media.AudioManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.HapticFeedbackConstants
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import android.widget.TextView

import androidx.appcompat.app.AlertDialog
import androidx.media3.common.util.UnstableApi
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat

import com.afollestad.aesthetic.Aesthetic.Companion.get
import com.afollestad.aesthetic.AestheticActivity
import com.meenbeese.chronos.Chronos
import com.meenbeese.chronos.R
import com.meenbeese.chronos.data.AlarmData
import com.meenbeese.chronos.data.PreferenceData
import com.meenbeese.chronos.data.SoundData
import com.meenbeese.chronos.data.TimerData
import com.meenbeese.chronos.dialogs.TimeChooserDialog
import com.meenbeese.chronos.dialogs.TimeChooserDialog.OnTimeChosenListener
import com.meenbeese.chronos.services.SleepReminderService.Companion.refreshSleepTime
import com.meenbeese.chronos.utils.FormatUtils
import com.meenbeese.chronos.utils.FormatUtils.format
import com.meenbeese.chronos.utils.FormatUtils.formatMillis
import com.meenbeese.chronos.utils.FormatUtils.formatUnit
import com.meenbeese.chronos.utils.FormatUtils.getShortFormat
import com.meenbeese.chronos.utils.ImageUtils.getBackgroundImage

import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.subscribeBy

import me.jfenn.slideactionview.SlideActionListener
import me.jfenn.slideactionview.SlideActionView

import java.util.Date
import java.util.concurrent.TimeUnit

import kotlin.math.min


class AlarmActivity : AestheticActivity(), SlideActionListener {
    private var overlay: View? = null
    private var time: TextView? = null
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
    private var textColorPrimaryInverseSubscription: Disposable? = null
    private var isDarkSubscription: Disposable? = null
    private var isDark = false

    @UnstableApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_alarm)
        chronos = applicationContext as Chronos
        overlay = findViewById(R.id.overlay)
        val date = findViewById<TextView>(R.id.date)
        time = findViewById(R.id.time)
        val actionView = findViewById<SlideActionView>(R.id.slideView)
        val disposables = CompositeDisposable()

        // Lock orientation
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LOCKED
        textColorPrimaryInverseSubscription = get()
            .textColorPrimaryInverse()
            .subscribeBy(
                onNext = { integer: Int? ->
                    overlay?.setBackgroundColor(integer!!)
                },
                onError = { it.printStackTrace() }
            ).also { disposables.add(it) }
        isDarkSubscription = get()
            .isDark
            .subscribeBy(
                onNext = { aBoolean: Boolean ->
                    isDark = aBoolean
                },
                onError = { it.printStackTrace() }
            ).also { disposables.add(it) }
        actionView.setLeftIcon(VectorDrawableCompat.create(resources, R.drawable.ic_snooze, theme))
        actionView.setRightIcon(VectorDrawableCompat.create(resources, R.drawable.ic_close, theme))
        actionView.setListener(this)
        isSlowWake = PreferenceData.SLOW_WAKE_UP.getValue(this)
        slowWakeMillis = PreferenceData.SLOW_WAKE_UP_TIME.getValue(this)
        val isAlarm = intent.hasExtra(EXTRA_ALARM)
        if (isAlarm) {
            alarm = intent.getParcelableExtra(EXTRA_ALARM)
            isVibrate = alarm?.isVibrate == true
            if (alarm?.hasSound() == true) sound = alarm?.getSound()
        } else if (intent.hasExtra(EXTRA_TIMER)) {
            val timer = intent.getParcelableExtra<TimerData>(EXTRA_TIMER)
            isVibrate = timer?.isVibrate == true
            if (timer?.hasSound() == true) sound = timer.sound
        } else finish()
        date.text = format(Date(), FormatUtils.FORMAT_DATE + ", " + getShortFormat(this))
        if (sound != null && !sound!!.isSetVolumeSupported) {
            // Use the backup method if it is not supported
            audioManager = getSystemService(AUDIO_SERVICE) as AudioManager
            originalVolume = audioManager!!.getStreamVolume(AudioManager.STREAM_ALARM)
            if (isSlowWake) {
                minVolume = 0
                volumeRange = originalVolume - minVolume
                currentVolume = minVolume
                audioManager?.setStreamVolume(AudioManager.STREAM_ALARM, minVolume, 0)
            }
        }
        vibrator = getSystemService(VIBRATOR_SERVICE) as Vibrator
        triggerMillis = System.currentTimeMillis()
        handler = Handler(Looper.getMainLooper())
        runnable = object : Runnable {
            override fun run() {
                val elapsedMillis = System.currentTimeMillis() - triggerMillis
                val text = formatMillis(elapsedMillis)
                time?.text = "-${text.dropLast(3)}"
                if (isVibrate) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) vibrator!!.vibrate(
                        VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE)
                    ) else vibrator?.vibrate(500)
                }
                if (sound != null && !sound!!.isPlaying(chronos!!)) sound!!.play(chronos!!)
                if (alarm != null && isSlowWake) {
                    val slowWakeProgress = elapsedMillis.toFloat() / slowWakeMillis
                    window.addFlags(WindowManager.LayoutParams.FLAGS_CHANGED)
                    if (sound != null && sound!!.isSetVolumeSupported) {
                        val newVolume = min(1f, slowWakeProgress)
                        sound?.setVolume(chronos!!, newVolume)
                    } else if (currentVolume < originalVolume) {
                        // Backup volume setting behavior
                        val newVolume = minVolume + min(
                            originalVolume.toFloat(),
                            slowWakeProgress * volumeRange
                        ).toInt()
                        if (newVolume != currentVolume) {
                            audioManager?.setStreamVolume(AudioManager.STREAM_ALARM, newVolume, 0)
                            currentVolume = newVolume
                        }
                    }
                }
                handler?.postDelayed(this, 1000)
            }
        }
        handler?.post(runnable!!)
        sound?.play(chronos!!)
        refreshSleepTime(chronos!!)
        if (PreferenceData.RINGING_BACKGROUND_IMAGE.getValue(this)) getBackgroundImage(
            (findViewById<View>(R.id.background) as ImageView)
        )
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        window.addFlags(
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                    or WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                    or WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                    or WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                    or WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        textColorPrimaryInverseSubscription?.dispose()
        isDarkSubscription?.dispose()
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

    override fun onSlideLeft() {
        val minutes = intArrayOf(2, 5, 10, 20, 30, 60)
        val names = arrayOfNulls<CharSequence>(minutes.size + 1)
        for (i in minutes.indices) {
            names[i] = formatUnit(this@AlarmActivity, minutes[i])
        }
        names[minutes.size] = getString(R.string.title_snooze_custom)
        stopAnnoyance()
        AlertDialog.Builder(
            this@AlarmActivity,
            if (isDark) R.style.Theme_AppCompat_Dialog_Alert else R.style.Theme_AppCompat_Light_Dialog_Alert
        )
            .setItems(names) { _: DialogInterface?, which: Int ->
                if (which < minutes.size) {
                    val timer: TimerData = chronos!!.newTimer()
                    timer.setDuration(
                        TimeUnit.MINUTES.toMillis(
                            minutes[which].toLong()
                        ), chronos
                    )
                    timer.setVibrate(this@AlarmActivity, isVibrate)
                    timer.setSound(this@AlarmActivity, sound)
                    timer[chronos] = ((this@AlarmActivity.getSystemService(ALARM_SERVICE) as AlarmManager?)!!)
                    chronos?.onTimerStarted()
                    finish()
                } else {
                    val timerDialog = TimeChooserDialog(this@AlarmActivity)
                    timerDialog.setListener(object : OnTimeChosenListener {
                        override fun onTimeChosen(hours: Int, minutes: Int, seconds: Int) {
                            val timer: TimerData? = chronos?.newTimer()
                            timer?.setVibrate(this@AlarmActivity, isVibrate)
                            timer?.setSound(this@AlarmActivity, sound)
                            timer?.setDuration(
                                (TimeUnit.HOURS.toMillis(hours.toLong())
                                        + TimeUnit.MINUTES.toMillis(minutes.toLong())
                                        + TimeUnit.SECONDS.toMillis(seconds.toLong())),
                                chronos
                            )
                            timer!![chronos] = ((getSystemService(ALARM_SERVICE) as AlarmManager?)!!)
                            chronos?.onTimerStarted()
                            finish()
                        }
                    })
                    timerDialog.show()
                }
            }
            .setNegativeButton(android.R.string.cancel
            ) { dialog: DialogInterface, _: Int -> dialog.dismiss() }
            .show()
        overlay?.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
    }

    override fun onSlideRight() {
        overlay?.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
        finish()
    }

    companion object {
        const val EXTRA_ALARM = "meenbeese.chronos.AlarmActivity.EXTRA_ALARM"
        const val EXTRA_TIMER = "meenbeese.chronos.AlarmActivity.EXTRA_TIMER"
    }
}
