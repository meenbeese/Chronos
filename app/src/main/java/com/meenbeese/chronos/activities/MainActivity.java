package com.meenbeese.chronos.activities;

import android.content.Intent;
import android.os.Bundle;
import android.provider.AlarmClock;
import android.provider.Settings;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.afollestad.aesthetic.AestheticActivity;

import java.lang.ref.WeakReference;

import com.meenbeese.chronos.Chronos;
import com.meenbeese.chronos.R;
import com.meenbeese.chronos.data.PreferenceData;
import com.meenbeese.chronos.dialogs.AlertDialog;
import com.meenbeese.chronos.fragments.BaseFragment;
import com.meenbeese.chronos.fragments.HomeFragment;
import com.meenbeese.chronos.fragments.SplashFragment;
import com.meenbeese.chronos.fragments.StopwatchFragment;
import com.meenbeese.chronos.fragments.TimerFragment;
import com.meenbeese.chronos.receivers.TimerReceiver;


public class MainActivity extends AestheticActivity implements FragmentManager.OnBackStackChangedListener, Chronos.ActivityListener {

    public static final String EXTRA_FRAGMENT = "com.meenbeese.chronos.MainActivity.EXTRA_FRAGMENT";
    public static final int FRAGMENT_TIMER = 0;
    public static final int FRAGMENT_STOPWATCH = 2;

    private Chronos chronos;
    private WeakReference<BaseFragment> fragmentRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        chronos = (Chronos) getApplicationContext();
        chronos.setListener(this);

        if (savedInstanceState == null) {
            BaseFragment fragment = createFragmentFor(getIntent());
            if (fragment == null)
                return;

            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fragment, fragment)
                    .commit();

            fragmentRef = new WeakReference<>(fragment);
        } else {
            BaseFragment fragment;

            if (fragmentRef == null || (fragment = fragmentRef.get()) == null)
                fragment = new HomeFragment();

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment, fragment)
                    .commit();

            fragmentRef = new WeakReference<>(fragment);
        }

        getSupportFragmentManager().addOnBackStackChangedListener(this);

        // background permissions info
        if (!PreferenceData.INFO_BACKGROUND_PERMISSIONS.getValue(this, false)) {
            AlertDialog alert = new AlertDialog(this);
            alert.setTitle(getString(R.string.info_background_permissions_title));
            alert.setContent(getString(R.string.info_background_permissions_body));
            alert.setListener((dialog, ok) -> {
                if (ok) {
                    PreferenceData.INFO_BACKGROUND_PERMISSIONS.setValue(MainActivity.this, true);
                    startActivity(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION));
                }
            });
            alert.show();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (isActionableIntent(intent)) {
            FragmentManager manager = getSupportFragmentManager();
            BaseFragment newFragment = createFragmentFor(intent);
            BaseFragment fragment = fragmentRef != null ? fragmentRef.get() : null;

            if (newFragment == null || newFragment.equals(fragment)) // check that fragment isn't already displayed
                return;

            if (newFragment instanceof HomeFragment && manager.getBackStackEntryCount() > 0) // clear the back stack
                manager.popBackStack(manager.getBackStackEntryAt(0).getId(), FragmentManager.POP_BACK_STACK_INCLUSIVE);

            FragmentTransaction transaction = manager.beginTransaction()
                    .setCustomAnimations(R.anim.slide_in_up_sheet, R.anim.slide_out_up_sheet, R.anim.slide_in_down_sheet, R.anim.slide_out_down_sheet)
                    .replace(R.id.fragment, newFragment);

            if (fragment instanceof HomeFragment && !(newFragment instanceof HomeFragment))
                transaction.addToBackStack(null);

            fragmentRef = new WeakReference<>(newFragment);
            transaction.commit();
        }
    }

    /**
     * Return a fragment to display the content provided by
     * the passed intent.
     *
     * @param intent    The intent passed to the activity.
     * @return          An instantiated fragment corresponding
     *                  to the passed intent.
     */
    @Nullable
    private BaseFragment createFragmentFor(Intent intent) {
        BaseFragment fragment = fragmentRef != null ? fragmentRef.get() : null;
        int fragmentId = intent.getIntExtra(EXTRA_FRAGMENT, -1);

        switch (fragmentId) {
            case FRAGMENT_STOPWATCH -> {
                if (fragment instanceof StopwatchFragment)
                    return fragment;
                return new StopwatchFragment();
            }
            case FRAGMENT_TIMER -> {
                if (intent.hasExtra(TimerReceiver.EXTRA_TIMER_ID)) {
                    int id = intent.getIntExtra(TimerReceiver.EXTRA_TIMER_ID, 0);
                    if (chronos.getTimers().size() <= id || id < 0)
                        return fragment;

                    Bundle args = new Bundle();
                    args.putParcelable(TimerFragment.EXTRA_TIMER, chronos.getTimers().get(id));

                    BaseFragment newFragment = new TimerFragment();
                    newFragment.setArguments(args);
                    return newFragment;
                }
                return fragment;
            }
            default -> {
                if (Intent.ACTION_MAIN.equals(intent.getAction()) || intent.getAction() == null)
                    return new SplashFragment();
                Bundle args = new Bundle();
                args.putString(HomeFragment.INTENT_ACTION, intent.getAction());
                BaseFragment newFragment = new HomeFragment();
                newFragment.setArguments(args);
                return newFragment;
            }
        }
    }

    /**
     * Determine if something needs to be done as a result
     * of the intent being sent to the activity - which has
     * a higher priority than any fragment that is currently
     * open.
     *
     * @param intent    The intent passed to the activity.
     * @return          True if a fragment should be replaced
     *                  with the action that this intent entails.
     */
    private boolean isActionableIntent(Intent intent) {
        return intent.hasExtra(EXTRA_FRAGMENT)
                || AlarmClock.ACTION_SHOW_ALARMS.equals(intent.getAction())
                || AlarmClock.ACTION_SET_TIMER.equals(intent.getAction())
                || AlarmClock.ACTION_SET_ALARM.equals(intent.getAction());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (chronos != null)
            chronos.setListener(null);

        chronos = null;
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onPause() {
        super.onPause();
        chronos.stopCurrentSound();
    }

    @Override
    public void onBackStackChanged() {
        BaseFragment fragment = (BaseFragment) getSupportFragmentManager().findFragmentById(R.id.fragment);
        fragmentRef = new WeakReference<>(fragment);
    }

    @Override
    public void requestPermissions(String... permissions) {
        ActivityCompat.requestPermissions(this, permissions, 0);
    }

    @Override
    public FragmentManager gettFragmentManager() {
        return getSupportFragmentManager();
    }
}
