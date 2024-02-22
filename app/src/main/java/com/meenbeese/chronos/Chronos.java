package com.meenbeese.chronos;

import android.content.Intent;
import android.graphics.Color;
import android.media.Ringtone;
import android.net.Uri;
import android.os.Build;
import android.widget.Toast;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;
import androidx.multidex.MultiDexApplication;

import com.afollestad.aesthetic.Aesthetic;
import com.afollestad.aesthetic.AutoSwitchMode;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.audio.AudioAttributes;
import com.google.android.exoplayer2.source.MediaSourceFactory;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import com.meenbeese.chronos.data.AlarmData;
import com.meenbeese.chronos.data.PreferenceData;
import com.meenbeese.chronos.data.SoundData;
import com.meenbeese.chronos.data.TimerData;
import com.meenbeese.chronos.services.SleepReminderService;
import com.meenbeese.chronos.services.TimerService;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;


public class Chronos extends MultiDexApplication implements Player.EventListener {

    public static final int THEME_DAY_NIGHT = 0;
    public static final int THEME_DAY = 1;
    public static final int THEME_NIGHT = 2;
    public static final int THEME_AMOLED = 3;

    public static final String NOTIFICATION_CHANNEL_STOPWATCH = "stopwatch";
    public static final String NOTIFICATION_CHANNEL_TIMERS = "timers";

    private Ringtone currentRingtone;

    private List<AlarmData> alarms;
    private List<TimerData> timers;

    private List<ChronosListener> listeners;
    private ActivityListener listener;

    private SimpleExoPlayer player;
    private HlsMediaSource.Factory hlsMediaSourceFactory;
    private String currentStream;

    @Override
    public void onCreate() {
        super.onCreate();

        listeners = new ArrayList<>();
        alarms = new ArrayList<>();
        timers = new ArrayList<>();

        player = new SimpleExoPlayer.Builder(this).build();
        player.addListener(this);

        DefaultDataSourceFactory dataSourceFactory = new DefaultDataSourceFactory(this, Util.getUserAgent(this, "exoplayer2example"), null);
        hlsMediaSourceFactory = new HlsMediaSource.Factory(dataSourceFactory);

        int alarmLength = PreferenceData.ALARM_LENGTH.getValue(this);
        for (int id = 0; id < alarmLength; id++) {
            alarms.add(new AlarmData(id, this));
        }

        int timerLength = PreferenceData.TIMER_LENGTH.getValue(this);
        for (int id = 0; id < timerLength; id++) {
            TimerData timer = new TimerData(id, this);
            if (timer.isSet())
                timers.add(timer);
        }

        if (timerLength > 0) {
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O) {
                startForegroundService(new Intent(this, TimerService.class));
            } else {
                startService(new Intent(this, TimerService.class));
            }
        }

        SleepReminderService.refreshSleepTime(this);
    }

    public List<AlarmData> getAlarms() {
        return alarms;
    }

    public List<TimerData> getTimers() {
        return timers;
    }

    /**
     * Create a new alarm, assigning it an unused preference id.
     *
     * @return          The newly instantiated [AlarmData](./data/AlarmData).
     */
    public AlarmData newAlarm() {
        AlarmData alarm = new AlarmData(alarms.size(), Calendar.getInstance());
        alarm.sound = SoundData.fromString(PreferenceData.DEFAULT_ALARM_RINGTONE.getValue(this, ""));
        alarms.add(alarm);
        onAlarmCountChanged();
        return alarm;
    }

    /**
     * Remove an alarm and all of its its preferences.
     *
     * @param alarm     The alarm to be removed.
     */
    public void removeAlarm(AlarmData alarm) {
        alarm.onRemoved(this);

        int index = alarms.indexOf(alarm);
        alarms.remove(index);
        for (int i = index; i < alarms.size(); i++) {
            alarms.get(i).onIdChanged(i, this);
        }

        onAlarmCountChanged();
        onAlarmsChanged();
    }

    /**
     * Update preferences to show that the alarm count has been changed.
     */
    public void onAlarmCountChanged() {
        PreferenceData.ALARM_LENGTH.setValue(this, alarms.size());
    }

    /**
     * Notify the application of changes to the current alarms.
     */
    public void onAlarmsChanged() {
        for (ChronosListener listener : listeners) {
            listener.onAlarmsChanged();
        }
    }

    /**
     * Create a new timer, assigning it an unused preference id.
     *
     * @return          The newly instantiated [TimerData](./data/TimerData).
     */
    public TimerData newTimer() {
        TimerData timer = new TimerData(timers.size());
        timers.add(timer);
        onTimerCountChanged();
        return timer;
    }

    /**
     * Remove a timer and all of its preferences.
     *
     * @param timer     The timer to be removed.
     */
    public void removeTimer(TimerData timer) {
        timer.onRemoved(this);

        int index = timers.indexOf(timer);
        timers.remove(index);
        for (int i = index; i < timers.size(); i++) {
            timers.get(i).onIdChanged(i, this);
        }

        onTimerCountChanged();
        onTimersChanged();
    }

    /**
     * Update the preferences to show that the timer count has been changed.
     */
    public void onTimerCountChanged() {
        PreferenceData.TIMER_LENGTH.setValue(this, timers.size());
    }

    /**
     * Notify the application of changes to the current timers.
     */
    public void onTimersChanged() {
        for (ChronosListener listener : listeners) {
            listener.onTimersChanged();
        }
    }

    /**
     * Starts the timer service after a timer has been set.
     */
    public void onTimerStarted() {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O) {
            startForegroundService(new Intent(this, TimerService.class));
        } else {
            startService(new Intent(this, TimerService.class));
        }
    }

    /**
     * Update the application theme.
     */
    public void updateTheme() {
        if (isNight()) {
            Aesthetic.Companion.get()
                    .isDark(true)
                    .lightStatusBarMode(AutoSwitchMode.OFF)
                    .colorPrimary(ContextCompat.getColor(this, R.color.colorNightPrimary))
                    .colorStatusBar(Color.TRANSPARENT)
                    .colorNavigationBar(ContextCompat.getColor(this, R.color.colorNightPrimaryDark))
                    .colorAccent(ContextCompat.getColor(this, R.color.colorNightAccent))
                    .colorCardViewBackground(ContextCompat.getColor(this, R.color.colorNightForeground))
                    .colorWindowBackground(ContextCompat.getColor(this, R.color.colorNightPrimaryDark))
                    .textColorPrimary(ContextCompat.getColor(this, R.color.textColorPrimaryNight))
                    .textColorSecondary(ContextCompat.getColor(this, R.color.textColorSecondaryNight))
                    .textColorPrimaryInverse(ContextCompat.getColor(this, R.color.textColorPrimary))
                    .textColorSecondaryInverse(ContextCompat.getColor(this, R.color.textColorSecondary))
                    .apply();
        } else {
            int theme = getActivityTheme();
            if (theme == THEME_DAY || theme == THEME_DAY_NIGHT) {
                Aesthetic.Companion.get()
                        .isDark(false)
                        .lightStatusBarMode(AutoSwitchMode.ON)
                        .colorPrimary(ContextCompat.getColor(this, R.color.colorPrimary))
                        .colorStatusBar(Color.TRANSPARENT)
                        .colorNavigationBar(ContextCompat.getColor(this, R.color.colorPrimaryDark))
                        .colorAccent(ContextCompat.getColor(this, R.color.colorAccent))
                        .colorCardViewBackground(ContextCompat.getColor(this, R.color.colorForeground))
                        .colorWindowBackground(ContextCompat.getColor(this, R.color.colorPrimaryDark))
                        .textColorPrimary(ContextCompat.getColor(this, R.color.textColorPrimary))
                        .textColorSecondary(ContextCompat.getColor(this, R.color.textColorSecondary))
                        .textColorPrimaryInverse(ContextCompat.getColor(this, R.color.textColorPrimaryNight))
                        .textColorSecondaryInverse(ContextCompat.getColor(this, R.color.textColorSecondaryNight))
                        .apply();
            } else if (theme == THEME_AMOLED) {
                Aesthetic.Companion.get()
                        .isDark(true)
                        .lightStatusBarMode(AutoSwitchMode.OFF)
                        .colorPrimary(Color.BLACK)
                        .colorStatusBar(Color.TRANSPARENT)
                        .colorNavigationBar(Color.BLACK)
                        .colorAccent(Color.WHITE)
                        .colorCardViewBackground(Color.BLACK)
                        .colorWindowBackground(Color.BLACK)
                        .textColorPrimary(Color.WHITE)
                        .textColorSecondary(Color.WHITE)
                        .textColorPrimaryInverse(Color.BLACK)
                        .textColorSecondaryInverse(Color.BLACK)
                        .apply();
            }
        }
    }

    /**
     * Determine if the theme should be a night theme.
     *
     * @return          True if the current theme is a night theme.
     */
    public boolean isNight() {
        int time = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        return ((time < getDayStart() || time > getDayEnd()) && getActivityTheme() == THEME_DAY_NIGHT) || getActivityTheme() == THEME_NIGHT;
    }

    /**
     * Get the theme to be used for activities and things. Despite
     * what the name implies, it does not return a theme resource,
     * but rather one of Chronos.THEME_DAY_NIGHT, Chronos.THEME_DAY,
     * Chronos.THEME_NIGHT, or Chronos.THEME_AMOLED.
     *
     * @return          The theme to be used for activities.
     */
    public int getActivityTheme() {
        return PreferenceData.THEME.getValue(this);
    }

    /**
     * @return the hour of the start of the day (24h), as specified by the user
     */
    public int getDayStart() {
        return PreferenceData.DAY_START.getValue(this);
    }

    /**
     * @return the hour of the end of the day (24h), as specified by the user
     */
    public int getDayEnd() {
        return PreferenceData.DAY_END.getValue(this);
    }

    /**
     * Determine if a ringtone is currently playing.
     *
     * @return          True if a ringtone is currently playing.
     */
    public boolean isRingtonePlaying() {
        return currentRingtone != null && currentRingtone.isPlaying();
    }

    public void playRingtone(Ringtone ringtone) {
        if (!ringtone.isPlaying()) {
            stopCurrentSound();
            ringtone.play();
        }

        currentRingtone = ringtone;
    }

    /**
     * Play a stream ringtone.
     *
     * @param url       The URL of the stream to be passed to ExoPlayer.
     * @see [ExoPlayer Repo](https://github.com/google/ExoPlayer)
     */
    private void playStream(String url, MediaSourceFactory factory) {
        stopCurrentSound();

        // Error handling, including when this is a progressive stream
        // rather than a HLS stream, is in onPlayerError
        player.prepare(factory.createMediaSource(Uri.parse(url)));
        player.setPlayWhenReady(true);

        currentStream = url;
    }

    /**
     * Play a stream ringtone.
     *
     * @param url       The URL of the stream to be passed to ExoPlayer.
     * @see [ExoPlayer Repo](https://github.com/google/ExoPlayer)
     */
    public void playStream(String url) {
        playStream(url, hlsMediaSourceFactory);
    }

    /**
     * Play a stream ringtone.
     *
     * @param url           The URL of the stream to be passed to ExoPlayer.
     * @param attributes    The attributes to play the stream with.
     * @see [ExoPlayer Repo](https://github.com/google/ExoPlayer)
     */
    public void playStream(String url, AudioAttributes attributes) {
        player.stop();
        player.setAudioAttributes(attributes);
        playStream(url);
    }

    /**
     * Stop the currently playing stream.
     */
    public void stopStream() {
        player.stop();
        currentStream = null;
    }

    /**
     * Sets the player volume to the given float.
     *
     * @param volume            The volume between 0 and 1
     */
    public void setStreamVolume(float volume) {
        player.setVolume(volume);
    }

    /**
     * Determine if the passed url matches the stream that is currently playing.
     *
     * @param url           The URL to match the current stream to.
     * @return              True if the URL matches that of the currently playing
     *                      stream.
     */
    public boolean isPlayingStream(String url) {
        return currentStream != null && currentStream.equals(url);
    }

    /**
     * Stop the currently playing sound, regardless of whether it is a ringtone
     * or a stream.
     */
    public void stopCurrentSound() {
        if (isRingtonePlaying())
            currentRingtone.stop();
        stopStream();
    }

    public void addListener(ChronosListener listener) {
        listeners.add(listener);
    }

    public void removeListener(ChronosListener listener) {
        listeners.remove(listener);
    }

    public void setListener(ActivityListener listener) {
        this.listener = listener;
        if (listener != null)
            updateTheme();
    }

    @Override
    public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
        switch (playbackState) {
            case Player.STATE_BUFFERING:
            case Player.STATE_READY:
            // We are idle while switching from HLS to Progressive streaming
            case Player.STATE_IDLE:
                break;
            default:
                currentStream = null;
                break;
        }
    }

    @Override
    public void onPlayerError(ExoPlaybackException error) {
        currentStream = null;
        Exception exception;
        switch (error.type) {
            case ExoPlaybackException.TYPE_RENDERER -> exception = error.getRendererException();
            case ExoPlaybackException.TYPE_UNEXPECTED -> exception = error.getUnexpectedException();
            case ExoPlaybackException.TYPE_SOURCE, ExoPlaybackException.TYPE_REMOTE -> exception = error.getSourceException();
            case ExoPlaybackException.TYPE_OUT_OF_MEMORY -> exception = new Exception("Out of memory exception.");
            default -> {
                return;
            }
        }

        exception.printStackTrace();
        Toast.makeText(this, exception.getClass().getName() + ": " + exception.getMessage(), Toast.LENGTH_SHORT).show();
    }

    public FragmentManager getFragmentManager() {
        if (listener != null)
            return listener.gettFragmentManager();
        else return null;
    }

    public interface ChronosListener {
        void onAlarmsChanged();

        void onTimersChanged();
    }

    public interface ActivityListener {
        FragmentManager gettFragmentManager(); //help
    }
}
