package com.meenbeese.chronos.data;

import android.media.AudioAttributes;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.media3.common.C;
import androidx.media3.common.util.UnstableApi;

import com.meenbeese.chronos.Chronos;


public class SoundData {

    private static final String SEPARATOR = ":ChronosSoundData:";

    public static final String TYPE_RINGTONE = "ringtone";

    private final String name;
    private final String type;
    private final String url;

    private Ringtone ringtone;

    public SoundData(String name, String type, String url) {
        this.name = name;
        this.type = type;
        this.url = url;
    }

    public SoundData(String name, String type, String url, Ringtone ringtone) {
        this(name, type, url);
        this.ringtone = ringtone;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public String getUrl() {
        return url;
    }

    /**
     * Plays the sound. This will pass the SoundData instance to the provided
     * [Chronos](../Chronos) class, which will store the currently playing sound
     * until it is stopped or cancelled.
     *
     * @param chronos           The active Application instance.
     */
    @UnstableApi
    public void play(Chronos chronos) {
        if (type.equals(TYPE_RINGTONE) && url.startsWith("content://")) {
            if (ringtone == null) {
                ringtone = RingtoneManager.getRingtone(chronos, Uri.parse(url));
                ringtone.setAudioAttributes(new AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .build());
            }

            chronos.playRingtone(ringtone);
        } else {
            chronos.playStream(url, type,
                    new androidx.media3.common.AudioAttributes.Builder()
                            .setUsage(C.USAGE_ALARM)
                            .build());
        }
    }

    /**
     * Stops the currently playing alarm. This only differentiates between sounds
     * if the sound is a ringtone; if it is a stream, then all streams will be stopped,
     * regardless of whether this sound is in fact the currently playing stream or not.
     *
     * @param chronos           The active Application instance.
     */
    public void stop(Chronos chronos) {
        if (ringtone != null)
            ringtone.stop();
        else chronos.stopStream();
    }

    /**
     * Preview the sound on the "media" volume channel.
     *
     * @param chronos           The active Application instance.
     */
    @UnstableApi
    public void preview(Chronos chronos) {
        if (url.startsWith("content://")) {
            if (ringtone == null) {
                ringtone = RingtoneManager.getRingtone(chronos, Uri.parse(url));
                ringtone.setAudioAttributes(new AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .build());
            }

            chronos.playRingtone(ringtone);
        } else {
            chronos.playStream(url, type,
                    new androidx.media3.common.AudioAttributes.Builder()
                            .setUsage(C.USAGE_ALARM)
                            .build());
        }
    }

    /**
     * Decide whether the sound is currently playing or not.
     *
     * @param chronos           The active Application instance.
     * @return                  True if "this" sound is playing.
     */
    public boolean isPlaying(Chronos chronos) {
        if (ringtone != null)
            return ringtone.isPlaying();
        else return chronos.isPlayingStream(url);
    }

    /**
     * Sets the player volume to the given float.
     *
     * @param chronos           The active Application instance.
     * @param volume            The volume between 0 and 1
     */
    public void setVolume(Chronos chronos, float volume) {
        if (ringtone != null)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                ringtone.setVolume(volume);
            } else {
                // Not possible
                throw new IllegalArgumentException("Attempted to set the ringtone volume on a device older than Android P.");
            }
        else chronos.setStreamVolume(volume);
    }

    /**
     * Is the setVolume method supported on this version of Android
     *
     * @return true if supported
     */
    public boolean isSetVolumeSupported() {
        return ringtone == null || Build.VERSION.SDK_INT >= Build.VERSION_CODES.P;
    }

    /**
     * Returns an identifier string that can be used to recreate this
     * SoundDate class.
     *
     * @return                  A non-null identifier string.
     */
    @NonNull
    @Override
    public String toString() {
        return name + SEPARATOR + type + SEPARATOR + url;
    }

    /**
     * Construct a new instance of SoundData from an identifier string which was
     * (hopefully) created by [toString](#tostring).
     *
     * @param string            A non-null identifier string.
     * @return                  A recreated SoundData instance.
     */
    @Nullable
    public static SoundData fromString(String string) {
        if (string.contains(SEPARATOR)) {
            String[] data = string.split(SEPARATOR);
            if (data.length == 3
                    && !data[0].isEmpty() && !data[1].isEmpty() && !data[2].isEmpty())
                return new SoundData(data[0], data[1], data[2]);
        }

        return null;
    }

    /**
     * Decide if two SoundData are equal.
     *
     * @param obj               The object to compare to.
     * @return                  True if the SoundData contain the same sound.
     */
    @Override
    public boolean equals(Object obj) {
        return (obj instanceof SoundData && ((SoundData) obj).url.equals(url));
    }
}
