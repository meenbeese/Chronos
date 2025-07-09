package com.meenbeese.chronos.ext

import android.content.Context
import android.media.RingtoneManager

import com.meenbeese.chronos.data.SoundData

fun loadRingtones(context: Context): List<SoundData> {
    val sounds = mutableListOf<SoundData>()
    val manager = RingtoneManager(context).apply {
        setType(RingtoneManager.TYPE_RINGTONE)
    }
    val cursor = manager.cursor
    if (cursor.count > 0 && cursor.moveToFirst()) {
        do {
            val title = cursor.getString(RingtoneManager.TITLE_COLUMN_INDEX)
            val uri = manager.getRingtoneUri(cursor.position).toString()
            sounds.add(SoundData(name = title, type = SoundData.TYPE_RINGTONE, uri = uri))
        } while (cursor.moveToNext())
    }
    return sounds
}
