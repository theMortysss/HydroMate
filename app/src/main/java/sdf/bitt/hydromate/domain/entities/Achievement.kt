package sdf.bitt.hydromate.domain.entities

import java.time.LocalDateTime

data class Achievement(
    val id: String,
    val title: String,
    val description: String,
    val icon: String,
    val isUnlocked: Boolean = false,
    val unlockedAt: LocalDateTime? = null
)