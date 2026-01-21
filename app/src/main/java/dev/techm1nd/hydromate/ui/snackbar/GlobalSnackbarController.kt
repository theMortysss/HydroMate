package dev.techm1nd.hydromate.ui.snackbar

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Глобальный контроллер для показа Snackbar сообщений по всему приложению
 * Позволяет показывать уведомления о достижениях, челленджах и других событиях
 * независимо от текущего экрана
 */
@Singleton
class GlobalSnackbarController @Inject constructor() {

    private val _messages = Channel<SnackbarMessage>(Channel.BUFFERED)
    val messages: Flow<SnackbarMessage> = _messages.receiveAsFlow()

    /**
     * Показать обычное сообщение
     */
    fun showMessage(message: String) {
        _messages.trySend(
            SnackbarMessage(
                message = message,
                type = SnackbarType.INFO
            )
        )
    }

    /**
     * Показать сообщение об успехе
     */
    fun showSuccess(message: String) {
        _messages.trySend(
            SnackbarMessage(
                message = message,
                type = SnackbarType.SUCCESS
            )
        )
    }

    /**
     * Показать сообщение об ошибке
     */
    fun showError(message: String) {
        _messages.trySend(
            SnackbarMessage(
                message = message,
                type = SnackbarType.ERROR
            )
        )
    }

    /**
     * Показать сообщение о достижении
     */
    fun showAchievement(title: String, description: String? = null) {
        val message = if (description != null) {
            "🏆 $title\n$description"
        } else {
            "🏆 $title"
        }

        _messages.trySend(
            SnackbarMessage(
                message = message,
                type = SnackbarType.ACHIEVEMENT,
                duration = SnackbarDuration.LONG
            )
        )
    }

    /**
     * Показать сообщение о повышении уровня
     */
    fun showLevelUp(level: Int, xpGained: Int) {
        _messages.trySend(
            SnackbarMessage(
                message = "🎊 Level Up!\nYou reached level $level (+${xpGained} XP)",
                type = SnackbarType.LEVEL_UP,
                duration = SnackbarDuration.LONG
            )
        )
    }

    /**
     * Показать сообщение о провале челленджа
     */
    fun showChallengeViolation(challengeName: String, drinkName: String) {
        _messages.trySend(
            SnackbarMessage(
                message = "⚠️ Challenge Failed!\n$challengeName violated by drinking $drinkName",
                type = SnackbarType.WARNING,
                duration = SnackbarDuration.LONG
            )
        )
    }

    /**
     * Показать сообщение о завершении челленджа
     */
    fun showChallengeCompleted(challengeName: String, xpGained: Int) {
        _messages.trySend(
            SnackbarMessage(
                message = "🎉 Challenge Completed!\n$challengeName (+${xpGained} XP)",
                type = SnackbarType.SUCCESS,
                duration = SnackbarDuration.LONG
            )
        )
    }

    /**
     * Показать сообщение о разблокировке персонажа
     */
    fun showCharacterUnlocked(characterName: String) {
        _messages.trySend(
            SnackbarMessage(
                message = "🎭 New Character Unlocked!\n$characterName is now available",
                type = SnackbarType.ACHIEVEMENT,
                duration = SnackbarDuration.LONG
            )
        )
    }

    /**
     * Показать сообщение о достижении цели
     */
    fun showGoalReached() {
        _messages.trySend(
            SnackbarMessage(
                message = "🎉 Daily Goal Reached!\nGreat job staying hydrated!",
                type = SnackbarType.SUCCESS,
                duration = SnackbarDuration.MEDIUM
            )
        )
    }
}

/**
 * Модель сообщения для Snackbar
 */
data class SnackbarMessage(
    val message: String,
    val type: SnackbarType = SnackbarType.INFO,
    val duration: SnackbarDuration = SnackbarDuration.SHORT,
    val actionLabel: String? = null,
    val onAction: (() -> Unit)? = null
)

/**
 * Типы Snackbar сообщений
 */
enum class SnackbarType {
    INFO,
    SUCCESS,
    ERROR,
    WARNING,
    ACHIEVEMENT,
    LEVEL_UP
}

/**
 * Длительность показа Snackbar
 */
enum class SnackbarDuration {
    SHORT,      // 2 секунды
    MEDIUM,     // 4 секунды
    LONG        // 6 секунд
}