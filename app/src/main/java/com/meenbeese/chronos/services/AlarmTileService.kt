package com.meenbeese.chronos.services

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Intent
import android.graphics.drawable.Icon
import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import android.text.format.DateFormat
import android.util.Log

import androidx.annotation.RequiresApi

import com.meenbeese.chronos.R
import com.meenbeese.chronos.activities.MainActivity
import com.meenbeese.chronos.data.AlarmData
import com.meenbeese.chronos.data.SoundData
import com.meenbeese.chronos.db.AlarmRepository
import com.meenbeese.chronos.utils.FormatUtils

import kotlinx.coroutines.runBlocking

import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

import java.util.Calendar

@RequiresApi(Build.VERSION_CODES.Q)
class AlarmTileService : TileService(), KoinComponent {

    private val repo: AlarmRepository by inject()

    override fun onStartListening() {
        super.onStartListening()
        updateTile()
    }

    override fun onClick() {
        val intent = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            val pendingIntent = PendingIntent.getActivity(
                this,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            startActivityAndCollapse(pendingIntent)
        } else {
            @Suppress("DEPRECATION")
            @SuppressLint("StartActivityAndCollapseDeprecated")
            startActivityAndCollapse(intent)
        }
    }

    private fun updateTile() {
        val tile = qsTile ?: return
        val now = Calendar.getInstance()

        val alarms: List<AlarmData> = runBlocking {
            repo.getAllDirect().map {
                AlarmData(
                    id = it.id,
                    name = it.name,
                    time = Calendar.getInstance().apply { timeInMillis = it.timeInMillis },
                    isEnabled = it.isEnabled,
                    days = it.days.toMutableList(),
                    isVibrate = it.isVibrate,
                    sound = it.sound?.let { sound -> SoundData.fromString(sound).getOrNull() }
                )
            }
        }

        alarms.forEach { alarm ->
            val next = alarm.getNext()
            Log.d(
                "AlarmTileService",
                "Loaded alarm: id=${alarm.id}, name=${alarm.name}, enabled=${alarm.isEnabled}, " +
                        "time=${alarm.time.time}, days=${alarm.days}, nextTrigger=${next?.time ?: "null"}"
            )
        }

        val nextAlarm = alarms
            .filter { it.isEnabled }
            .mapNotNull { alarm -> alarm.getNext()?.let { nextTime -> alarm to nextTime } }
            .filter { it.second.after(now) }
            .minByOrNull { it.second.timeInMillis }
            ?.first

        Log.d("AlarmTileService", "Next alarm selected: $nextAlarm")

        tile.label = getString(R.string.next_alarm)

        tile.subtitle = nextAlarm?.let {
            val next = it.getNext()
            if (next != null) {
                val dayLabel = DateFormat.format("EEE", next).toString()
                val timeLabel = FormatUtils.formatShort(this, next.time)
                "$dayLabel • $timeLabel" // e.g., "Wed • 7:00 AM"
            } else null
        } ?: getString(R.string.word_none)

        tile.icon = Icon.createWithResource(this, R.drawable.ic_snooze)
        tile.state = if (nextAlarm != null) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
        tile.updateTile()
    }
}
