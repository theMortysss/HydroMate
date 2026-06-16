package dev.techm1nd.hydromate.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.techm1nd.hydromate.domain.entities.SyncStatus
import dev.techm1nd.hydromate.domain.entities.User
import java.time.format.DateTimeFormatter

@Composable
fun AccountSettingsCard(
    currentUser: User?,
    syncStatus: SyncStatus,
    onSyncNow: () -> Unit,
    onSignOut: () -> Unit,
    onLinkAccount: () -> Unit,
    onEditProfile: () -> Unit,
    isSyncEnabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    val isAnonymous = currentUser?.isAnonymous == true

    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = if (isAnonymous) {
                MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
            } else {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            }
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Аккаунт",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                if (isAnonymous) {
                    AssistChip(
                        onClick = onLinkAccount,
                        label = { Text("Привязать аккаунт") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                            labelColor = MaterialTheme.colorScheme.onErrorContainer
                        )
                    )
                }
            }

            HorizontalDivider()

            // User Info
            if (currentUser != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isAnonymous) "Анонимный пользователь" else currentUser.displayNameOrEmail,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium
                        )

                        if (!isAnonymous && currentUser.email != null) {
                            Text(
                                text = currentUser.email,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    if (!isAnonymous) {
                        IconButton(onClick = onEditProfile) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Редактировать профиль"
                            )
                        }
                    }
                }
            }

            // Anonymous Warning
            if (isAnonymous) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Привяжите аккаунт",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                            Text(
                                text = "Ваши данные сохраняются только локально. Привяжите email или Google, чтобы синхронизировать данные между устройствами.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                }
            }

            HorizontalDivider()

//            // Sync Status (only for registered users)
//            if (!isAnonymous && isSyncEnabled) {
//                Row(
//                    modifier = Modifier.fillMaxWidth(),
//                    horizontalArrangement = Arrangement.SpaceBetween,
//                    verticalAlignment = Alignment.CenterVertically
//                ) {
//                    Column(modifier = Modifier.weight(1f)) {
//                        Text(
//                            text = "Статус синхронизации",
//                            style = MaterialTheme.typography.bodyMedium,
//                            fontWeight = FontWeight.Medium
//                        )
//
//                        when (syncStatus) {
//                            is SyncStatus.Idle -> Text(
//                                text = "Синхронизация ещё не выполнялась",
//                                style = MaterialTheme.typography.bodySmall,
//                                color = MaterialTheme.colorScheme.onSurfaceVariant
//                            )
//                            is SyncStatus.Syncing -> Row(
//                                horizontalArrangement = Arrangement.spacedBy(8.dp),
//                                verticalAlignment = Alignment.CenterVertically
//                            ) {
//                                CircularProgressIndicator(
//                                    modifier = Modifier.size(16.dp),
//                                    strokeWidth = 2.dp
//                                )
//                                Text(
//                                    text = "Синхронизация...",
//                                    style = MaterialTheme.typography.bodySmall,
//                                    color = MaterialTheme.colorScheme.primary
//                                )
//                            }
//                            is SyncStatus.Success -> Text(
//                                text = "Последняя синхронизация: ${syncStatus.syncedAt.format(
//                                    DateTimeFormatter.ofPattern("dd MMM, HH:mm")
//                                )}",
//                                style = MaterialTheme.typography.bodySmall,
//                                color = MaterialTheme.colorScheme.tertiary
//                            )
//                            is SyncStatus.Error -> Text(
//                                text = "Ошибка: ${syncStatus.message}",
//                                style = MaterialTheme.typography.bodySmall,
//                                color = MaterialTheme.colorScheme.error
//                            )
//                        }
//                    }
//
//                    if (syncStatus !is SyncStatus.Syncing) {
//                        IconButton(onClick = onSyncNow) {
//                            Icon(
//                                imageVector = Icons.Default.Refresh,
//                                contentDescription = "Синхронизировать сейчас"
//                            )
//                        }
//                    }
//                }
//
//                HorizontalDivider()
//            }

            // Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (isAnonymous) {
                    FilledTonalButton(
                        onClick = onLinkAccount,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Link,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Привязать аккаунт")
                    }
                }

                OutlinedButton(
                    onClick = onSignOut,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Logout,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (isAnonymous) "Выйти" else "Выйти из аккаунта")
                }
            }
        }
    }
}