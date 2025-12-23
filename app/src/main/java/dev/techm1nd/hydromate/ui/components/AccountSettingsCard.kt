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
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            Text(
                text = "Account",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Divider()

            // User info
            if (currentUser != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (currentUser.isAnonymous) {
                            Icons.Default.Person
                        } else {
                            Icons.Default.AccountCircle
                        },
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (currentUser.isAnonymous) {
                                "Anonymous User"
                            } else {
                                currentUser.displayNameOrEmail
                            },
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium
                        )

                        if (!currentUser.isAnonymous && currentUser.email != null) {
                            Text(
                                text = currentUser.email,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        }
                    }
                }

                if (!currentUser.isAnonymous) {
                    Button(onClick = onEditProfile) {
                        Icon(Icons.Default.Edit, null)
                        Spacer(Modifier.width(8.dp))
                        Text("Edit Profile")
                    }
                }

                // Anonymous warning
                if (currentUser.isAnonymous) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onErrorContainer
                            )
                            Text(
                                text = "Link your account to save progress across devices",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }

                    // Link account button
                    OutlinedButton(
                        onClick = onLinkAccount,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Link, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Link Account")
                    }
                }
            }

            Divider()

            // Sync status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Sync Status",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Medium
                    )

                    Text(
                        text = when (syncStatus) {
                            is SyncStatus.Idle -> "Ready to sync"
                            is SyncStatus.Syncing -> "Syncing..."
                            is SyncStatus.Success -> "Last sync: ${
                                syncStatus.syncedAt.format(
                                    DateTimeFormatter.ofPattern("MMM dd, HH:mm")
                                )
                            }"
                            is SyncStatus.Error -> "Error: ${syncStatus.message}"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = when (syncStatus) {
                            is SyncStatus.Error -> MaterialTheme.colorScheme.error
                            else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        }
                    )
                }

                IconButton(
                    onClick = onSyncNow,
                    enabled = syncStatus !is SyncStatus.Syncing
                ) {
                    Icon(
                        imageVector = Icons.Default.Sync,
                        contentDescription = "Sync now"
                    )
                }
            }

            Divider()

            // Sign out button
            OutlinedButton(
                onClick = onSignOut,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Icon(Icons.Default.Logout, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Sign Out")
            }
        }
    }
}
