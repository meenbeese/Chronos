package com.meenbeese.chronos.data

import android.os.Parcelable

import kotlinx.parcelize.Parcelize

@Parcelize
data class SoundData(
    val name: String,
    val type: String,
    val url: String
) : Parcelable
