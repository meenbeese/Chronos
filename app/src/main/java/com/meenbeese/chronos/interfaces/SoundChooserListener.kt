package com.meenbeese.chronos.interfaces

import com.meenbeese.chronos.data.SoundData

interface SoundChooserListener {
    fun onSoundChosen(sound: SoundData?)
}
