package dev.techm1nd.hydromate.ui.snackbar

import androidx.annotation.StringRes
import dev.techm1nd.hydromate.R
import dev.techm1nd.hydromate.utils.UiText
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

    fun showMessage(message: UiText) {
        _messages.trySend(
            SnackbarMessage(
                message = message,
                type = SnackbarType.INFO
            )
        )
    }

    fun showSuccess(message: UiText) {
        _messages.trySend(
            SnackbarMessage(
                message = message,
                type = SnackbarType.SUCCESS
            )
        )
    }

    fun showError(message: UiText) {
        _messages.trySend(
            SnackbarMessage(
                message = message,
                type = SnackbarType.ERROR
            )
        )
    }

    fun showAchievement(
        title: String,
        description: String? = null
    ) {
        _messages.trySend(
            SnackbarMessage(
                message = if (description != null) {
                    UiText.StringResource(
                        R.string.snackbar_achievement_with_description,
                        listOf(title, description)
                    )
                } else {
                    UiText.StringResource(
                        R.string.snackbar_achievement,
                        listOf(title)
                    )
                },
                type = SnackbarType.ACHIEVEMENT,
                duration = SnackbarDuration.LONG
            )
        )
    }

    fun showLevelUp(
        level: Int,
        xpGained: Int
    ) {
        _messages.trySend(
            SnackbarMessage(
                message = UiText.StringResource(
                    R.string.snackbar_level_up,
                    listOf(level, xpGained)
                ),
                type = SnackbarType.LEVEL_UP,
                duration = SnackbarDuration.LONG
            )
        )
    }

    fun showChallengeViolation(
        challengeName: String,
        drinkName: String
    ) {
        _messages.trySend(
            SnackbarMessage(
                message = UiText.StringResource(
                    R.string.snackbar_challenge_failed,
                    listOf(challengeName, drinkName)
                ),
                type = SnackbarType.WARNING,
                duration = SnackbarDuration.LONG
            )
        )
    }

    fun showChallengeCompleted(
        challengeName: String,
        xpGained: Int
    ) {
        _messages.trySend(
            SnackbarMessage(
                message = UiText.StringResource(
                    R.string.snackbar_challenge_completed,
                    listOf(challengeName, xpGained)
                ),
                type = SnackbarType.SUCCESS,
                duration = SnackbarDuration.LONG
            )
        )
    }

    fun showCharacterUnlocked(
        characterName: String
    ) {
        _messages.trySend(
            SnackbarMessage(
                message = UiText.StringResource(
                    R.string.snackbar_character_unlocked,
                    listOf(characterName)
                ),
                type = SnackbarType.ACHIEVEMENT,
                duration = SnackbarDuration.LONG
            )
        )
    }

    fun showGoalReached() {
        _messages.trySend(
            SnackbarMessage(
                message = UiText.StringResource(
                    R.string.snackbar_goal_reached
                ),
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
    val message: UiText,
    val type: SnackbarType = SnackbarType.INFO,
    val duration: SnackbarDuration = SnackbarDuration.SHORT,
    val actionLabel: UiText? = null,
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