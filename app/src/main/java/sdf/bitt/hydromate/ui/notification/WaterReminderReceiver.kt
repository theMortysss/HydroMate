package sdf.bitt.hydromate.ui.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import sdf.bitt.hydromate.domain.repositories.DrinkRepository
import sdf.bitt.hydromate.domain.usecases.CalculateHydrationUseCase
import sdf.bitt.hydromate.domain.usecases.GetTodayProgressUseCase
import sdf.bitt.hydromate.domain.usecases.GetUserSettingsUseCase
import javax.inject.Inject
import androidx.core.content.edit

@AndroidEntryPoint
class WaterReminderReceiver : BroadcastReceiver() {

    @Inject
    lateinit var notificationManager: HydroMateNotificationManager

    @Inject
    lateinit var getTodayProgressUseCase: GetTodayProgressUseCase

    @Inject
    lateinit var getUserSettingsUseCase: GetUserSettingsUseCase

    @Inject
    lateinit var notificationScheduler: NotificationScheduler

    @Inject
    lateinit var drinkRepository: DrinkRepository

    @Inject
    lateinit var calculateHydrationUseCase: CalculateHydrationUseCase

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == ACTION_WATER_REMINDER) {
            scope.launch {
                try {
                    val settings = getUserSettingsUseCase().first()

                    // Проверяем, включены ли уведомления
                    if (!settings.notificationsEnabled) {
                        Log.d(TAG, "Notifications disabled, skipping reminder")
                        return@launch
                    }

                    val progress = getTodayProgressUseCase().first()

                    // Рассчитываем текущую гидратацию
                    val drinks = drinkRepository.getAllActiveDrinks().first()
                    val drinksMap = drinks.associateBy { it.id }

                    val hydration = if (progress.entries.isNotEmpty()) {
                        calculateHydrationUseCase.calculateTotal(progress.entries, drinksMap)
                    } else {
                        null
                    }

                    // Определяем текущее количество в зависимости от настроек
                    val currentAmount = if (settings.showNetHydration) {
                        hydration?.netHydration ?: 0
                    } else {
                        progress.totalAmount
                    }

                    val adjustedGoal = progress.goalAmount

                    // НОВАЯ ЛОГИКА: Проверяем, достигнута ли цель
                    val isGoalReached = currentAmount >= adjustedGoal

                    if (isGoalReached) {
                        Log.d(TAG, "Goal already reached ($currentAmount/$adjustedGoal), skipping reminder")

                        // Отправляем поздравительное уведомление один раз
                        if (!hasShownCongratulationsToday(context)) {
                            notificationManager.showGoalAchievedNotification(
                                currentAmount = currentAmount,
                                goalAmount = adjustedGoal
                            )
                            markCongratulationsShown(context)
                        }

                        // Планируем следующее напоминание на завтра
                        notificationScheduler.scheduleNextDayReminder(settings)
                        return@launch
                    }

                    // Цель не достигнута - показываем обычное напоминание
                    Log.d(TAG, "Showing reminder: $currentAmount/$adjustedGoal")
                    notificationManager.showHydrationReminder(
                        currentAmount = currentAmount,
                        goalAmount = adjustedGoal
                    )

                    // Планируем следующее напоминание согласно интервалу
                    notificationScheduler.scheduleNotifications(settings)

                } catch (e: Exception) {
                    Log.e(TAG, "Error processing reminder", e)
                }
            }
        }
    }

    private fun hasShownCongratulationsToday(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val lastCongratulation = prefs.getLong(KEY_LAST_CONGRATULATION, 0)
        val today = System.currentTimeMillis() / (24 * 60 * 60 * 1000)
        val lastDay = lastCongratulation / (24 * 60 * 60 * 1000)
        return today == lastDay
    }

    private fun markCongratulationsShown(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit { putLong(KEY_LAST_CONGRATULATION, System.currentTimeMillis()) }
    }

    companion object {
        const val ACTION_WATER_REMINDER = "sdf.bitt.hydromate.ACTION_WATER_REMINDER"
        private const val TAG = "WaterReminderReceiver"
        private const val PREFS_NAME = "hydro_mate_notifications"
        private const val KEY_LAST_CONGRATULATION = "last_congratulation_time"
    }
}