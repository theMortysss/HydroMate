package dev.techm1nd.hydromate

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import dev.techm1nd.hydromate.domain.entities.AuthState
import dev.techm1nd.hydromate.domain.usecases.setting.GetUserSettingsUseCase
import dev.techm1nd.hydromate.ui.navigation.HydroMateNavigation
import dev.techm1nd.hydromate.ui.notification.NotificationScheduler
import dev.techm1nd.hydromate.ui.screens.auth.AuthViewModel
import dev.techm1nd.hydromate.ui.snackbar.GlobalSnackbarController
import dev.techm1nd.hydromate.ui.snackbar.GlobalSnackbarHost
import dev.techm1nd.hydromate.ui.theme.HydroMateTheme
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var notificationScheduler: NotificationScheduler

    @Inject
    lateinit var getUserSettingsUseCase: GetUserSettingsUseCase

    @Inject
    lateinit var globalSnackbarController: GlobalSnackbarController

    private var showPermissionRationale by mutableStateOf(false)
    private var showExactAlarmInfo by mutableStateOf(false)

    private var keepSplashScreen = true

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            checkExactAlarmPermission()
            initializeNotifications()
        } else {
            showPermissionRationale = true
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()

        splashScreen.setKeepOnScreenCondition { keepSplashScreen }

        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            HydroMateTheme {
                val authViewModel: AuthViewModel = hiltViewModel()
                val authUiState by authViewModel.state.collectAsStateWithLifecycle()

                LaunchedEffect(authUiState.isLoading) {
                    if (!authUiState.isLoading) {
                        kotlinx.coroutines.delay(50)
                        keepSplashScreen = false
                    }
                }

                Box(modifier = Modifier.fillMaxSize()) {
                    HydroMateNavigation(globalSnackbarController)

                    if (showPermissionRationale) {
                        PermissionRationaleDialog(
                            onDismiss = { showPermissionRationale = false },
                            onOpenSettings = {
                                openAppSettings()
                                showPermissionRationale = false
                            }
                        )
                    }

                    if (showExactAlarmInfo) {
                        ExactAlarmInfoDialog(
                            onDismiss = { showExactAlarmInfo = false },
                            onOpenSettings = {
                                openExactAlarmSettings()
                                showExactAlarmInfo = false
                            }
                        )
                    }
                }
            }
        }

        checkAndRequestPermissions()
    }

    private fun checkAndRequestPermissions() {
        lifecycleScope.launch {
            try {
                val settings = getUserSettingsUseCase().first()
                if (settings.notificationsEnabled && !hasRequestedPermissions()) {
                    checkNotificationPermission()
                    markPermissionsRequested()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun initializeNotifications() {
        lifecycleScope.launch {
            try {
                val settings = getUserSettingsUseCase().first()
                if (settings.notificationsEnabled) {
                    notificationScheduler.scheduleNotifications(settings)
                    android.util.Log.d("MainActivity", "Notifications initialized")
                }
            } catch (e: Exception) {
                android.util.Log.e("MainActivity", "Failed to initialize notifications", e)
            }
        }
    }

    private fun checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> {
                    checkExactAlarmPermission()
                }
                shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS) -> {
                    showPermissionRationale = true
                }
                else -> {
                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        } else {
            checkExactAlarmPermission()
        }
    }

    private fun checkExactAlarmPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = getSystemService(ALARM_SERVICE) as android.app.AlarmManager
            if (!alarmManager.canScheduleExactAlarms()) {
                showExactAlarmInfo = true
            }
        }
    }

    private fun openAppSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", packageName, null)
        }
        startActivity(intent)
    }

    private fun openExactAlarmSettings() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                data = Uri.fromParts("package", packageName, null)
            }
            startActivity(intent)
        }
    }

    private fun hasRequestedPermissions(): Boolean {
        val prefs = getSharedPreferences("app_prefs", MODE_PRIVATE)
        return prefs.getBoolean("permissions_requested", false)
    }

    private fun markPermissionsRequested() {
        val prefs = getSharedPreferences("app_prefs", MODE_PRIVATE)
        prefs.edit().putBoolean("permissions_requested", true).apply()
    }
}

@Composable
fun PermissionRationaleDialog(
    onDismiss: () -> Unit,
    onOpenSettings: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Text("🔔", style = MaterialTheme.typography.headlineLarge)
        },
        title = {
            Text(stringResource(R.string.perm_notif_title))
        },
        text = {
            Text(
                stringResource(R.string.perm_notif_text)
            )
        },
        confirmButton = {
            TextButton(onClick = onOpenSettings) {
                Text(stringResource(R.string.open_settings))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.skip))
            }
        }
    )
}

@Composable
fun ExactAlarmInfoDialog(
    onDismiss: () -> Unit,
    onOpenSettings: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Text("⏰", style = MaterialTheme.typography.headlineLarge)
        },
        title = {
            Text(stringResource(R.string.precise_alarms_title))
        },
        text = {
            Text(
                stringResource(R.string.precise_alarms_text)
            )
        },
        confirmButton = {
            TextButton(onClick = onOpenSettings) {
                Text(stringResource(R.string.enable))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.skiip))
            }
        }
    )
}