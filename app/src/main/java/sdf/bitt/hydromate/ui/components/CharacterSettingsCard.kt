package sdf.bitt.hydromate.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import sdf.bitt.hydromate.domain.entities.CharacterType

@Composable
fun CharacterSettingsCard(
    selectedCharacter: CharacterType,
    onCharacterClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    SettingsCard(
        title = "Character",
        icon = "🎭",
        modifier = modifier
    ) {
        ListItem(
            headlineContent = { Text("Your Hydration Buddy") },
            supportingContent = { Text("Choose your favorite character companion") },
            leadingContent = {
                Text(
                    text = selectedCharacter.emoji,
                    fontSize = 32.sp
                )
            },
            trailingContent = {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = selectedCharacter.displayName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = "Change character",
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            },
            modifier = Modifier.clickable { onCharacterClick() }
        )
    }
}
