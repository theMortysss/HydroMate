package dev.techm1nd.hydromate.data.repository

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import dev.techm1nd.hydromate.domain.repositories.TipsRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TipsRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : TipsRepository {

    private val prefs: SharedPreferences = context.getSharedPreferences(
        "hydration_tips_prefs",
        Context.MODE_PRIVATE
    )

    private val _viewedTipIds = MutableStateFlow(loadViewedTips())

    override fun getViewedTipIds(): Flow<Set<String>> = _viewedTipIds.asStateFlow()

    override suspend fun markTipAsViewed(tipId: String): Result<Unit> {
        return try {
            val currentViewed = _viewedTipIds.value.toMutableSet()
            currentViewed.add(tipId)

            prefs.edit {
                putStringSet(KEY_VIEWED_TIPS, currentViewed)
            }

            _viewedTipIds.value = currentViewed
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun loadViewedTips(): Set<String> {
        return prefs.getStringSet(KEY_VIEWED_TIPS, emptySet()) ?: emptySet()
    }

    companion object {
        private const val KEY_VIEWED_TIPS = "viewed_tips"
    }
}