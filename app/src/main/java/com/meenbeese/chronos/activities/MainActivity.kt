package com.meenbeese.chronos.activities

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.WindowManager

import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.media3.common.util.UnstableApi
import androidx.navigation.compose.rememberNavController

import com.meenbeese.chronos.data.Preferences
import com.meenbeese.chronos.ui.dialogs.BackgroundWarnDialog
import com.meenbeese.chronos.receivers.TimerReceiver
import com.meenbeese.chronos.db.TimerAlarmRepository
import com.meenbeese.chronos.ext.getFlow
import com.meenbeese.chronos.nav.NavGraph
import com.meenbeese.chronos.nav.TimerRoute
import com.meenbeese.chronos.nav.WatchRoute
import com.meenbeese.chronos.ui.theme.ChronosTheme
import com.meenbeese.chronos.ui.theme.ThemeFactory
import com.meenbeese.chronos.ui.theme.ThemeMode
import com.meenbeese.chronos.utils.AudioManager

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

import org.koin.android.ext.android.inject

@UnstableApi
class MainActivity : ComponentActivity() {
    private val repo: TimerAlarmRepository by inject()
    private val audioUtils: AudioManager by inject()

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // Permission granted
        } else {
            // Permission denied
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen()
        enableEdgeToEdge()

        window.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )

        setContent {
            val colorSeed by Preferences.COLOR_SEED.getFlow(this)
                .collectAsState(initial = Preferences.COLOR_SEED.get(this))
            val themeMode by Preferences.THEME.getFlow(this).map { ThemeMode.fromInt(it) }
                .collectAsState(initial = ThemeMode.fromInt(Preferences.THEME.get(this)))
            val dynamicColor by Preferences.DYNAMIC_COLOR.getFlow(this)
                .collectAsState(initial = Preferences.DYNAMIC_COLOR.get(this))

            ChronosTheme(
                customColorScheme = ThemeFactory.getSchemeFromSeed(
                    color = colorSeed,
                    dark = themeMode.isDark()
                ),
                dynamicColor = dynamicColor
            ) {
                val navController = rememberNavController()
                val showDialog = remember { mutableStateOf(false) }

                Box(Modifier.fillMaxSize()) {

                    NavGraph(navController = navController)

                    if (showDialog.value) {
                        BackgroundWarnDialog(
                            onDismiss = { showDialog.value = false },
                            onConfirm = {
                                CoroutineScope(Dispatchers.IO).launch {
                                    Preferences.INFO_BACKGROUND_PERMISSIONS.set(this@MainActivity, true)
                                    withContext(Dispatchers.Main) {
                                        showDialog.value = false
                                        startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION))
                                    }
                                }
                            }
                        )
                    }

                    LaunchedEffect(Unit) {
                        if (!Preferences.INFO_BACKGROUND_PERMISSIONS.get(this@MainActivity)) {
                            showDialog.value = true
                        }
                    }
                }

                onBackPressedDispatcher.addCallback(this@MainActivity, object : OnBackPressedCallback(true) {
                    override fun handleOnBackPressed() {
                        if (!navController.popBackStack()) finish()
                    }
                })

                LaunchedEffect(intent) {
                    val extraFragment = intent.getIntExtra(EXTRA_FRAGMENT, -1)
                    val isTimerIntent = extraFragment == FRAGMENT_TIMER && intent.hasExtra(TimerReceiver.EXTRA_TIMER_ID)
                    val isStopwatchIntent = extraFragment == FRAGMENT_STOPWATCH

                    if (isTimerIntent || isStopwatchIntent) {
                        when (extraFragment) {
                            FRAGMENT_STOPWATCH -> navController.navigate(WatchRoute)
                            FRAGMENT_TIMER -> {
                                val timerId = intent.getIntExtra(TimerReceiver.EXTRA_TIMER_ID, 0)
                                repo.timers.getOrNull(timerId)?.let { timer ->
                                    navController.navigate(TimerRoute(timer.id))
                                }
                            }
                        }
                    }
                }
            }
        }

        requestNotificationPermissionIfNeeded()
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                notificationPermissionLauncher.launch(
                    Manifest.permission.POST_NOTIFICATIONS
                )
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
    }

    @UnstableApi
    override fun onPause() {
        super.onPause()
        audioUtils.stopCurrentSound()
    }

    companion object {
        const val EXTRA_FRAGMENT = "com.meenbeese.chronos.MainActivity.EXTRA_FRAGMENT"
        const val FRAGMENT_TIMER = 0
        const val FRAGMENT_STOPWATCH = 2
    }
}
