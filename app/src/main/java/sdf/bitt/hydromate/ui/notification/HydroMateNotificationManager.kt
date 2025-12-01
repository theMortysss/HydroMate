package sdf.bitt.hydromate.ui.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import sdf.bitt.hydromate.MainActivity
import sdf.bitt.hydromate.R
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HydroMateNotificationManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val notificationManager = NotificationManagerCompat.from(context)

    companion object {
        const val CHANNEL_ID_REMINDERS = "hydro_mate_reminders"
        const val CHANNEL_ID_ACHIEVEMENTS = "hydro_mate_achievements"
        const val CHANNEL_NAME_REMINDERS = "Hydration Reminders"
        const val CHANNEL_NAME_ACHIEVEMENTS = "Achievements"
        const val NOTIFICATION_ID_REMINDER = 1001
        const val NOTIFICATION_ID_ACHIEVEMENT = 1002
    }

    init {
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Channel для напоминаний
            val reminderChannel = NotificationChannel(
                CHANNEL_ID_REMINDERS,
                CHANNEL_NAME_REMINDERS,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Reminds you to drink water regularly"
                enableVibration(true)
                setShowBadge(true)
            }

            // Channel для достижений
            val achievementChannel = NotificationChannel(
                CHANNEL_ID_ACHIEVEMENTS,
                CHANNEL_NAME_ACHIEVEMENTS,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifies about achieved hydration goals"
                enableVibration(true)
                setShowBadge(true)
                enableLights(true)
            }

            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(reminderChannel)
            manager.createNotificationChannel(achievementChannel)
        }
    }

    /**
     * Показывает обычное напоминание о необходимости выпить воду
     */
    fun showHydrationReminder(currentAmount: Int, goalAmount: Int) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val remainingAmount = (goalAmount - currentAmount).coerceAtLeast(0)
        val progressPercentage = ((currentAmount.toFloat() / goalAmount) * 100).toInt()

        val (title, message) = getReminderContent(progressPercentage, remainingAmount)

        val notification = NotificationCompat.Builder(context, CHANNEL_ID_REMINDERS)
            .setSmallIcon(R.drawable.ic_water_drop)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setProgress(100, progressPercentage, false)
            .build()

        try {
            notificationManager.notify(NOTIFICATION_ID_REMINDER, notification)
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }

    /**
     * Показывает поздравительное уведомление при достижении цели
     */
    fun showGoalAchievedNotification(currentAmount: Int, goalAmount: Int) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val overachievement = currentAmount - goalAmount
        val message = if (overachievement > 0) {
            "You've exceeded your goal by ${overachievement}ml! Keep up the amazing work! 💪"
        } else {
            "You've reached your daily hydration goal of ${goalAmount}ml! Great job staying healthy! 🌟"
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID_ACHIEVEMENTS)
            .setSmallIcon(R.drawable.ic_water_drop)
            .setContentTitle("🎉 Daily Goal Achieved!")
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setCategory(NotificationCompat.CATEGORY_STATUS)
            .build()

        try {
            // Отменяем предыдущие напоминания
            notificationManager.cancel(NOTIFICATION_ID_REMINDER)
            // Показываем поздравление
            notificationManager.notify(NOTIFICATION_ID_ACHIEVEMENT, notification)
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }

    private fun getReminderContent(progressPercentage: Int, remainingAmount: Int): Pair<String, String> {
        return when {
            progressPercentage >= 90 -> Pair(
                "💪 Almost There!",
                "You're so close! Just ${remainingAmount}ml to reach your goal!"
            )
            progressPercentage >= 75 -> Pair(
                "🌊 Great Progress!",
                "You're doing amazing! ${remainingAmount}ml remaining to reach your goal"
            )
            progressPercentage >= 50 -> Pair(
                "💧 Keep Going!",
                "Halfway there! Drink some water - ${remainingAmount}ml remaining"
            )
            progressPercentage >= 25 -> Pair(
                "🥤 Time to Hydrate!",
                "Don't forget to drink water! ${remainingAmount}ml remaining"
            )
            else -> Pair(
                "💦 Stay Hydrated!",
                "Time for a water break! ${remainingAmount}ml to reach your goal"
            )
        }
    }

    fun cancelAllNotifications() {
        notificationManager.cancel(NOTIFICATION_ID_REMINDER)
        notificationManager.cancel(NOTIFICATION_ID_ACHIEVEMENT)
    }

    fun cancelReminderNotifications() {
        notificationManager.cancel(NOTIFICATION_ID_REMINDER)
    }
}