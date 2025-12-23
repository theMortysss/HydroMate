package dev.techm1nd.hydromate.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.toColorInt
import dev.techm1nd.hydromate.domain.entities.Drink

@Composable
fun DrinkBreakdownCard(
    drinkBreakdown: Map<Drink, Int>,
    totalAmount: Int,
    showNetHydration: Boolean = false,
    totalDehydration: Int = 0,
    modifier: Modifier = Modifier
) {
    if (drinkBreakdown.isEmpty()) {
        return
    }

    val breakdownDisplayed = remember(drinkBreakdown, showNetHydration, totalDehydration) {
        if (showNetHydration) {
            val dehydDrinks = drinkBreakdown.filter { it.key.containsCaffeine || it.key.containsAlcohol }
            val sumRawDehyd = dehydDrinks.values.sum().toFloat()
            drinkBreakdown.mapValues { (drink, raw) ->
                val effective = raw * drink.hydrationMultiplier
                if ((drink.containsCaffeine || drink.containsAlcohol) && sumRawDehyd > 0) {
                    effective - (totalDehydration * (raw.toFloat() / sumRawDehyd))
                } else {
                    effective
                }
            }
        } else {
            drinkBreakdown.mapValues { (_, value) -> value.toFloat() }
        }
    }

    val sortedDrinks = remember(breakdownDisplayed) {
        breakdownDisplayed.entries
            .sortedByDescending { it.value }
            .take(10) // Показываем топ 10 напитков
    }

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                text = "Drink Breakdown",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Pie chart
            if (sortedDrinks.size > 1) {
                DrinkPieChart(
                    drinkData = sortedDrinks,
                    totalAmount = totalAmount.toFloat(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .padding(bottom = 16.dp)
                )
            }

            // List of drinks
            sortedDrinks.forEach { (drink, amount) ->
                DrinkBreakdownItem(
                    drink = drink,
                    amount = amount,
                    totalAmount = totalAmount.toFloat(),
                    modifier = Modifier.fillMaxWidth()
                )

                if (drink != sortedDrinks.last().key) {
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
private fun DrinkPieChart(
    drinkData: List<Map.Entry<Drink, Float>>,
    totalAmount: Float,
    modifier: Modifier = Modifier
) {
    val animatedProgress by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(1000, easing = EaseOutCubic),
        label = "pie_animation"
    )

    val primaryColor = MaterialTheme.colorScheme.primary
    Canvas(modifier = modifier) {
        val centerX = size.width / 2
        val centerY = size.height / 2
        val radius = minOf(centerX, centerY) * 0.8f

        var currentAngle = -90f // Start from top

        drinkData.forEach { (drink, amount) ->
            val percentage = amount / totalAmount
            val sweepAngle = 360f * percentage * animatedProgress

            val color = try {
                Color(drink.color.toColorInt())
            } catch (e: Exception) {
                primaryColor
            }

            // Draw pie slice
            drawArc(
                color = color.copy(alpha = 0.8f),
                startAngle = currentAngle,
                sweepAngle = sweepAngle,
                useCenter = true,
                topLeft = Offset(centerX - radius, centerY - radius),
                size = Size(radius * 2, radius * 2)
            )

            // Draw border
            drawArc(
                color = color,
                startAngle = currentAngle,
                sweepAngle = sweepAngle,
                useCenter = true,
                topLeft = Offset(centerX - radius, centerY - radius),
                size = Size(radius * 2, radius * 2),
                style = Stroke(width = 2.dp.toPx())
            )

            currentAngle += sweepAngle
        }

        // Draw center circle (donut effect)
        drawCircle(
            color = Color.White,
            radius = radius * 0.5f,
            center = Offset(centerX, centerY)
        )
    }
}

@Composable
private fun DrinkBreakdownItem(
    drink: Drink,
    amount: Float,
    totalAmount: Float,
    modifier: Modifier = Modifier
) {
    val percentage = (amount / totalAmount * 100).toInt()

    val animatedProgress by animateFloatAsState(
        targetValue = percentage / 100f,
        animationSpec = tween(800, easing = EaseOutCubic),
        label = "bar_animation"
    )

    val drinkColor = try {
        Color(drink.color.toColorInt())
    } catch (e: Exception) {
        MaterialTheme.colorScheme.primary
    }

    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = drink.icon,
                        fontSize = 24.sp
                    )
                    Column {
                        Text(
                            text = drink.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            if (drink.containsCaffeine) {
                                Text(
                                    text = "☕",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                            if (drink.containsAlcohol) {
                                Text(
                                    text = "🍺",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                }

                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = "${amount.toInt()}ml",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = drinkColor
                    )
                    Text(
                        text = "$percentage%",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Progress bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
            ) {
                // Background
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    shape = RoundedCornerShape(4.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {}

                // Progress
                Surface(
                    modifier = Modifier
                        .fillMaxWidth(animatedProgress)
                        .fillMaxHeight(),
                    shape = RoundedCornerShape(4.dp),
                    color = drinkColor
                ) {}
            }
        }
    }
}