package com.meenbeese.chronos.activities

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.AlarmClock
import android.provider.Settings
import android.view.ViewGroup
import android.view.WindowManager

import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentContainerView
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.commit
import androidx.media3.common.util.UnstableApi

import com.meenbeese.chronos.R
import com.meenbeese.chronos.data.Preferences
import com.meenbeese.chronos.ui.dialogs.BackgroundWarnDialog
import com.meenbeese.chronos.nav.destinations.HomeFragment
import com.meenbeese.chronos.nav.destinations.StopwatchFragment
import com.meenbeese.chronos.nav.destinations.TimerFragment
import com.meenbeese.chronos.receivers.TimerReceiver
import com.meenbeese.chronos.db.TimerAlarmRepository
import com.meenbeese.chronos.utils.AudioUtils

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

import org.koin.android.ext.android.inject

@UnstableApi
class MainActivity : AppCompatActivity(), FragmentManager.OnBackStackChangedListener {
    private val repo: TimerAlarmRepository by inject()
    private val audioUtils: AudioUtils by inject()

    private var fragmentRef: Fragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen()
        enableEdgeToEdge()

        window.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )

        setContent {
            val showDialog = remember { mutableStateOf(false) }

            Box(Modifier.fillMaxSize()) {
                AndroidView(
                    modifier = Modifier.matchParentSize(),
                    factory = { context ->
                        FragmentContainerView(context).apply {
                            id = R.id.fragment
                            layoutParams = ViewGroup.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT
                            )
                            post {
                                if (savedInstanceState == null) {
                                    val fragment = createFragmentFor(intent) ?: return@post
                                    supportFragmentManager.beginTransaction()
                                        .add(id, fragment)
                                        .commitNow()
                                    fragmentRef = fragment
                                }
                            }
                        }
                    }
                )

                // Background permissions info
                if (showDialog.value) {
                    BackgroundWarnDialog(
                        onDismiss = { showDialog.value = false },
                        onConfirm = {
                            CoroutineScope(Dispatchers.IO).launch {
                                Preferences.INFO_BACKGROUND_PERMISSIONS.set(this@MainActivity, true)
                                withContext(Dispatchers.Main) {
                                    showDialog.value = false
                                    val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION)
                                    startActivity(intent)
                                }
                            }
                        }
                    )
                }

                // Show background dialog if needed
                LaunchedEffect(Unit) {
                    if (!Preferences.INFO_BACKGROUND_PERMISSIONS.get(this@MainActivity)) {
                        showDialog.value = true
                    }
                }
            }
        }

        supportFragmentManager.addOnBackStackChangedListener(this)

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val fragmentManager = supportFragmentManager
                if (fragmentManager.backStackEntryCount > 0) {
                    fragmentManager.popBackStack()
                } else {
                    finish()
                }
            }
        })

        requestNotificationPermissionIfNeeded()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        if (isActionableIntent(intent)) {
            val manager = supportFragmentManager
            val newFragment = createFragmentFor(intent)
            val fragment = fragmentRef
            if (newFragment == null || newFragment == fragment) return

            if (newFragment is HomeFragment && manager.backStackEntryCount > 0) {
                manager.popBackStack(
                    manager.getBackStackEntryAt(0).id,
                    FragmentManager.POP_BACK_STACK_INCLUSIVE
                )
            }

            manager.commit {
                setCustomAnimations(
                    R.anim.slide_in_up_sheet,
                    R.anim.slide_out_up_sheet,
                    R.anim.slide_in_down_sheet,
                    R.anim.slide_out_down_sheet
                )
                replace(R.id.fragment, newFragment)
                if (fragment is HomeFragment && newFragment !is HomeFragment) {
                    addToBackStack(null)
                }
            }
            fragmentRef = newFragment
        }
    }

    /**
     * Return a fragment to display the content provided by
     * the passed intent.
     *
     * @param intent    The intent passed to the activity.
     * @return          An instantiated fragment corresponding
     * to the passed intent.
     */
    private fun createFragmentFor(intent: Intent): Fragment? {
        val fragment = if (fragmentRef != null) fragmentRef else null
        return when (intent.getIntExtra(EXTRA_FRAGMENT, -1)) {
            FRAGMENT_STOPWATCH -> {
                fragment as? StopwatchFragment ?: StopwatchFragment()
            }

            FRAGMENT_TIMER -> {
                if (intent.hasExtra(TimerReceiver.EXTRA_TIMER_ID)) {
                    val id = intent.getIntExtra(TimerReceiver.EXTRA_TIMER_ID, 0)
                    if (repo.timers.size <= id || id < 0) return fragment
                    val args = Bundle()
                    args.putParcelable(TimerFragment.EXTRA_TIMER, repo.timers[id])
                    val newFragment: Fragment = TimerFragment()
                    newFragment.arguments = args
                    return newFragment
                }
                fragment
            }

            else -> {
                val args = Bundle()
                args.putString(HomeFragment.INTENT_ACTION, intent.action)
                val newFragment: Fragment = HomeFragment()
                newFragment.arguments = args
                newFragment
            }
        }
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    REQUEST_NOTIFICATION_PERMISSION
                )
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
     * with the action that this intent entails.
     */
    private fun isActionableIntent(intent: Intent): Boolean {
        return intent.hasExtra(EXTRA_FRAGMENT)
                || AlarmClock.ACTION_SHOW_ALARMS == intent.action
                || AlarmClock.ACTION_SET_TIMER == intent.action
                || AlarmClock.ACTION_SET_ALARM == intent.action
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
    }

    @UnstableApi
    override fun onPause() {
        super.onPause()
        audioUtils.stopCurrentSound()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_NOTIFICATION_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted
            } else {
                // Permission denied
            }
        }
    }

    override fun onBackStackChanged() {
        val fragment = supportFragmentManager.findFragmentById(R.id.fragment)
        fragmentRef = fragment
    }

    companion object {
        const val EXTRA_FRAGMENT = "com.meenbeese.chronos.MainActivity.EXTRA_FRAGMENT"
        const val FRAGMENT_TIMER = 0
        const val FRAGMENT_STOPWATCH = 2
        private const val REQUEST_NOTIFICATION_PERMISSION = 1001
    }
}
