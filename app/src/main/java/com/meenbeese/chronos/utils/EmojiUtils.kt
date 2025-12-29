package com.meenbeese.chronos.utils

object EmojiUtils {
    private const val EMOJI_PRESENT: Byte = 0x01
    private const val EMOJI_ABSENT: Byte = 0x00

    fun encodeAlarmName(emoji: String?, name: String): String {
        val nameBytes = name.toByteArray(Charsets.UTF_8)

        val data = if (emoji.isNullOrEmpty()) {
            byteArrayOf(EMOJI_ABSENT) + nameBytes
        } else {
            val emojiBytes = emoji.toByteArray(Charsets.UTF_8)

            if (emojiBytes.size > 127) {
                return name
            }

            byteArrayOf(
                EMOJI_PRESENT,
                emojiBytes.size.toByte()
            ) + emojiBytes + nameBytes
        }

        return data.toString(Charsets.ISO_8859_1)
    }

    fun decodeAlarmName(raw: String?): Pair<String?, String> {
        if (raw.isNullOrEmpty()) return null to ""

        val bytes = raw.toByteArray(Charsets.ISO_8859_1)

        if (bytes.size < 2 || bytes[0] != EMOJI_PRESENT) {
            return null to raw
        }

        val emojiLen = bytes[1].toInt() and 0xFF
        val emojiStart = 2
        val emojiEnd = emojiStart + emojiLen

        if (emojiLen == 0 || emojiEnd > bytes.size) {
            return null to raw
        }

        val emoji = bytes
            .copyOfRange(emojiStart, emojiEnd)
            .toString(Charsets.UTF_8)

        val name = bytes
            .copyOfRange(emojiEnd, bytes.size)
            .toString(Charsets.UTF_8)

        return emoji to name
    }
}
