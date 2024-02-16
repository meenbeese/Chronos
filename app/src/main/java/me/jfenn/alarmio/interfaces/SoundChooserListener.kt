package me.jfenn.alarmio.interfaces

import me.jfenn.alarmio.data.SoundData


interface SoundChooserListener {
    fun onSoundChosen(sound: SoundData?)
}
