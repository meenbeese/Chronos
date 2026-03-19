package com.meenbeese.chronos.data

import android.os.Parcelable

import arrow.core.Option
import arrow.core.None
import arrow.core.Some

import kotlinx.parcelize.Parcelize

@Parcelize
class SoundData(
    val name: String,
    val type: String,
    val url: String
) : Parcelable {
    /**
     * Returns an identifier string that can be used to recreate this
     * SoundDate class.
     *
     * @return                  A non-null identifier string.
     */
    override fun toString(): String {
        return name + SEPARATOR + type + SEPARATOR + url
    }

    /**
     * Decide if two SoundData are equal.
     *
     * @param other               The object to compare to.
     * @return                  True if the SoundData contain the same sound.
     */
    override fun equals(other: Any?): Boolean {
        return (other is SoundData && other.url == url)
    }

    override fun hashCode(): Int {
        return 31 * (31 * (31 * name.hashCode() + type.hashCode()) + url.hashCode())
    }

    companion object {
        private const val SEPARATOR = ":ChronosSoundData:"
        const val TYPE_RINGTONE: String = "ringtone"

        /**
         * Construct a new instance of SoundData from an identifier string which was
         * (hopefully) created by [toString](#tostring).
         *
         * @param string            A non-null identifier string.
         * @return                  A recreated SoundData instance.
         */
        @JvmStatic
        fun fromString(string: String): Option<SoundData> {
            val data = string.split(SEPARATOR).takeIf { it.size == 3 } ?: return None
            val (name, type, url) = data
            return Some(SoundData(name, type, url))
        }
    }
}
