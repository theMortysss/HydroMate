package dev.techm1nd.hydromate.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import dev.techm1nd.hydromate.domain.entities.HydrationTip
import dev.techm1nd.hydromate.domain.entities.TipCategory

/**
 * Горизонтальная полоса с кружками-историями
 */
@Composable
fun HydrationTipsStories(
    viewedTipIds: Set<String>,
    onTipViewed: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTip by remember { mutableStateOf<HydrationTip?>(null) }
    val allTips = remember { HydrationTip.getAllTips() }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Group tips by category to count unviewed categories
            val categoriesWithUnviewed = TipCategory.entries.toTypedArray().count { category ->
                val categoryTips = allTips.filter { it.category == category }
                categoryTips.any { !viewedTipIds.contains(it.id) }
            }

            if (categoriesWithUnviewed > 0) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = "$categoriesWithUnviewed new",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }

        // Stories Row
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(horizontal = 4.dp)
        ) {
            // Group tips by category
            val categoriesWithTips = TipCategory.entries.mapNotNull { category ->
                val categoryTips = allTips.filter { it.category == category }
                if (categoryTips.isNotEmpty()) category to categoryTips else null
            }

            items(categoriesWithTips) { (category, tips) ->
                val hasUnviewed = tips.any { !viewedTipIds.contains(it.id) }
                val firstTip = tips.first()

                TipStoryCircle(
                    category = category,
                    hasUnviewed = hasUnviewed,
                    onClick = { selectedTip = firstTip }
                )
            }
        }
    }

    // Story Dialog
    selectedTip?.let { tip ->
        TipStoryDialog(
            tip = tip,
            allTipsInCategory = allTips.filter { it.category == tip.category },
            viewedTipIds = viewedTipIds,
            onDismiss = { selectedTip = null },
            onTipViewed = onTipViewed,
            onNavigate = { newTip -> selectedTip = newTip }
        )
    }
}

/**
 * Кружок категории с анимацией непросмотренных
 */
@Composable
private fun TipStoryCircle(
    category: TipCategory,
    hasUnviewed: Boolean,
    onClick: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier
            .width(72.dp)
            .clickable(onClick = onClick)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(64.dp)
                .then(
                    if (hasUnviewed) {
                        Modifier.border(
                            width = 3.dp,
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    Color(category.color.removePrefix("#").toLong(16) or 0xFF000000),
                                    Color(category.color
                                        .removePrefix("#")
                                        .toLong(16) or 0xFF000000).copy(alpha = 0.5f)
                                )
                            ),
                            shape = CircleShape
                        )
                    } else {
                        Modifier.border(
                            width = 2.dp,
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                            shape = CircleShape
                        )
                    }
                )
                .padding(4.dp)
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .then(if (hasUnviewed) Modifier else Modifier),
                shape = CircleShape,
                color = Color(category.color.removePrefix("#").toLong(16) or 0xFF000000)
                    .copy(alpha = 0.15f)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Text(
                        text = category.icon,
                        fontSize = 28.sp
                    )
                }
            }
        }

        Text(
            text = category.displayName,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
    }
}

/**
 * Диалог с советом (как история)
 */
@Composable
private fun TipStoryDialog(
    tip: HydrationTip,
    allTipsInCategory: List<HydrationTip>,
    viewedTipIds: Set<String>,
    onDismiss: () -> Unit,
    onTipViewed: (String) -> Unit,
    onNavigate: (HydrationTip) -> Unit
) {
    var currentTipIndex by remember(tip) {
        mutableIntStateOf(allTipsInCategory.indexOf(tip))
    }

    val currentTip = allTipsInCategory[currentTipIndex]

    LaunchedEffect(currentTip.id) {
        if (!viewedTipIds.contains(currentTip.id)) {
            onTipViewed(currentTip.id)
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .statusBarsPadding()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Header with progress indicators
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Progress bars
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        allTipsInCategory.forEachIndexed { index, _ ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(3.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (index <= currentTipIndex)
                                            MaterialTheme.colorScheme.primary
                                        else
                                            MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                                    )
                            )
                        }
                    }

                    // Close button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        IconButton(onClick = onDismiss) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = MaterialTheme.colorScheme.onBackground
                            )
                        }
                    }
                }

                // Content
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Category badge
                    Surface(
                        shape = CircleShape,
                        color = Color(
                            currentTip.category.color
                                .removePrefix("#")
                                .toLong(16) or 0xFF000000
                        ).copy(alpha = 0.15f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = currentTip.category.icon,
                                fontSize = 20.sp
                            )
                            Text(
                                text = currentTip.category.displayName,
                                style = MaterialTheme.typography.labelLarge,
                                color = Color(
                                    currentTip.category.color
                                        .removePrefix("#")
                                        .toLong(16) or 0xFF000000
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    // Title
                    Text(
                        text = currentTip.title,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Content
                    Text(
                        text = currentTip.content,
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
                        lineHeight = 24.sp
                    )

                    // Actionable advice
                    currentTip.actionableAdvice?.let { advice ->
                        Spacer(modifier = Modifier.height(24.dp))

                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(
                                    alpha = 0.5f
                                )
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text(
                                    text = "💡",
                                    fontSize = 24.sp
                                )
                                Text(
                                    text = advice,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    lineHeight = 20.sp
                                )
                            }
                        }
                    }

                    // Source
                    currentTip.source?.let { source ->
                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "Source: $source",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                            textAlign = TextAlign.Center
                        )
                    }
                }

                // Navigation buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Previous
                    TextButton(
                        onClick = {
                            if (currentTipIndex > 0) {
                                currentTipIndex--
                            }
                        },
                        enabled = currentTipIndex > 0
                    ) {
                        Text("← Previous")
                    }

                    // Counter
                    Text(
                        text = "${currentTipIndex + 1} / ${allTipsInCategory.size}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                        modifier = Modifier.align(Alignment.CenterVertically)
                    )

                    // Next
                    TextButton(
                        onClick = {
                            if (currentTipIndex < allTipsInCategory.size - 1) {
                                currentTipIndex++
                            } else {
                                onDismiss()
                            }
                        }
                    ) {
                        Text(if (currentTipIndex < allTipsInCategory.size - 1) "Next →" else "Done")
                    }
                }
            }
        }
    }
}
