package dev.techm1nd.hydromate.ui.snackbar

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.zIndex
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.hazeEffect
import kotlinx.coroutines.delay

/**
 * Глобальный Snackbar Host для показа сообщений по всему приложению
 * Использует красивый дизайн с эффектом размытия
 */
@Composable
fun GlobalSnackbarHost(
    snackbarController: GlobalSnackbarController,
    modifier: Modifier = Modifier,
    hazeState: HazeState = remember { HazeState() }
) {
    var currentMessage by remember { mutableStateOf<SnackbarMessage?>(null) }
    var isVisible by remember { mutableStateOf(false) }

    // Слушаем новые сообщения
    LaunchedEffect(snackbarController) {
        snackbarController.messages.collect { message ->
            // Скрываем текущее сообщение если есть
            if (isVisible) {
                isVisible = false
                delay(300) // Ждем завершения анимации
            }

            // Показываем новое сообщение
            currentMessage = message
            isVisible = true

            // Автоматически скрываем через время
            val delayMillis = when (message.duration) {
                SnackbarDuration.SHORT -> 2000L
                SnackbarDuration.MEDIUM -> 4000L
                SnackbarDuration.LONG -> 6000L
            }

            delay(delayMillis)
            isVisible = false
            delay(300)
            currentMessage = null
        }
    }

    // Анимированный контейнер
    Box(
        modifier = modifier
            .fillMaxSize()
            .zIndex(1f), // Поверх всего контента
        contentAlignment = Alignment.TopCenter
    ) {
        AnimatedVisibility(
            visible = isVisible,
            enter = slideInVertically(
                initialOffsetY = { -it },
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            ) + fadeIn(),
            exit = slideOutVertically(
                targetOffsetY = { -it },
                animationSpec = tween(300)
            ) + fadeOut()
        ) {
            currentMessage?.let { message ->
                EnhancedSnackbar(
                    message = message,
                    hazeState = hazeState,
                    modifier = Modifier.padding(top = 12.dp, start = 32.dp, end = 32.dp)
                )
            }
        }
    }
}

/**
 * Красивый Snackbar с эффектом размытия и цветовой кодировкой
 */
@Composable
private fun EnhancedSnackbar(
    message: SnackbarMessage,
    hazeState: HazeState,
    modifier: Modifier = Modifier
) {
    val backgroundColor = when (message.type) {
        SnackbarType.SUCCESS -> MaterialTheme.colorScheme.primary
        SnackbarType.ERROR -> MaterialTheme.colorScheme.error
        SnackbarType.WARNING -> Color(0xFFFF9800) // Orange
        SnackbarType.ACHIEVEMENT -> Color(0xFFFFD700) // Gold
        SnackbarType.LEVEL_UP -> Color(0xFF9C27B0) // Purple
        SnackbarType.INFO -> MaterialTheme.colorScheme.primary
    }

    val textColor = when (message.type) {
        SnackbarType.ACHIEVEMENT -> Color.Black
        else -> MaterialTheme.colorScheme.onBackground
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .hazeEffect(
                state = hazeState,
                style = HazeStyle(
                    backgroundColor = backgroundColor,
                    tint = HazeTint(
                        color = backgroundColor.copy(alpha = 0.7f),
                        blendMode = BlendMode.Src
                    ),
                    blurRadius = 30.dp,
                )
            )
            .border(
                width = 1.dp,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.8f),
                        Color.White.copy(alpha = 0.2f),
                    ),
                ),
                shape = RoundedCornerShape(16.dp)
            )
            .padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Иконка анимированная (пульсация для достижений)
            if (message.type == SnackbarType.ACHIEVEMENT || message.type == SnackbarType.LEVEL_UP) {
                PulsingIcon(
                    text = when (message.type) {
                        SnackbarType.ACHIEVEMENT -> "🏆"
                        SnackbarType.LEVEL_UP -> "🎊"
                        else -> ""
                    }
                )
            }

            Text(
                text = message.message,
                color = textColor,
                fontSize = 16.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f)
            )

            // Кнопка действия если есть
            message.actionLabel?.let { label ->
                TextButton(
                    onClick = { message.onAction?.invoke() },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = textColor
                    )
                ) {
                    Text(label)
                }
            }
        }
    }
}

/**
 * Пульсирующая иконка для важных событий
 */
@Composable
private fun PulsingIcon(text: String) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Box(
        modifier = Modifier
            .size(32.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = 24.sp
        )
    }
}