package sdf.bitt.hydromate

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
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import sdf.bitt.hydromate.domain.usecases.GetUserSettingsUseCase
import sdf.bitt.hydromate.ui.navigation.HydroMateNavigation
import sdf.bitt.hydromate.ui.notification.NotificationScheduler
import sdf.bitt.hydromate.ui.theme.HydroMateTheme
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var notificationScheduler: NotificationScheduler

    @Inject
    lateinit var getUserSettingsUseCase: GetUserSettingsUseCase

    private var showPermissionRationale by mutableStateOf(false)
    private var showExactAlarmInfo by mutableStateOf(false)

    // Launcher для запроса разрешения на уведомления
    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // Разрешение получено, проверяем exact alarm
            checkExactAlarmPermission()
        } else {
            // Разрешение отклонено
            showPermissionRationale = true
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            HydroMateTheme {
                Box(modifier = Modifier.fillMaxSize()) {
                    HydroMateNavigation()

                    // Диалог с объяснением необходимости разрешения
                    if (showPermissionRationale) {
                        PermissionRationaleDialog(
                            onDismiss = { showPermissionRationale = false },
                            onOpenSettings = {
                                openAppSettings()
                                showPermissionRationale = false
                            }
                        )
                    }

                    // Диалог с информацией о exact alarm
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

        // Запрашиваем разрешения при первом запуске
        checkAndRequestPermissions()
    }

    private fun checkAndRequestPermissions() {
        lifecycleScope.launch {
            try {
                val settings = getUserSettingsUseCase().first()

                // Запрашиваем разрешения только если уведомления включены
                if (settings.notificationsEnabled) {
                    checkNotificationPermission()
                }
            } catch (e: Exception) {
                e.printStackTrace()
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
                    // Разрешение уже есть, проверяем exact alarm
                    checkExactAlarmPermission()
                }
                shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS) -> {
                    // Показываем объяснение, почему нужно разрешение
                    showPermissionRationale = true
                }
                else -> {
                    // Запрашиваем разрешение
                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        } else {
            // Для Android 12 и ниже уведомления работают по умолчанию
            checkExactAlarmPermission()
        }
    }

    private fun checkExactAlarmPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = getSystemService(ALARM_SERVICE) as android.app.AlarmManager
            if (!alarmManager.canScheduleExactAlarms()) {
                // Показываем информацию о необходимости включить exact alarms
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
            Text("Notification Permission Required")
        },
        text = {
            Text(
                "HydroMate needs notification permission to remind you to drink water throughout the day. " +
                        "This helps you stay hydrated and reach your daily goals.\n\n" +
                        "You can enable it in the app settings."
            )
        },
        confirmButton = {
            TextButton(onClick = onOpenSettings) {
                Text("Open Settings")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Maybe Later")
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
            Text("Precise Reminders")
        },
        text = {
            Text(
                "For the most accurate hydration reminders, please allow HydroMate to " +
                        "schedule exact alarms.\n\n" +
                        "This ensures you get reminded at the right times throughout the day."
            )
        },
        confirmButton = {
            TextButton(onClick = onOpenSettings) {
                Text("Enable")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Skip")
            }
        }
    )
}