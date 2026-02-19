package com.zettl.vocabuhero.data.prefs

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.util.Calendar

private val Context.dataStore: DataStore<androidx.datastore.preferences.core.Preferences> by preferencesDataStore(name = "settings")

class SettingsStore(private val context: Context) {
    private val newCardsPerDayKey = intPreferencesKey("new_cards_per_day")
    private val dailyReviewLimitKey = intPreferencesKey("daily_review_limit")
    private val lastReviewDayKey = longPreferencesKey("last_review_day")
    private val streakKey = intPreferencesKey("streak")
    private val firstLaunchKey = longPreferencesKey("first_launch")
    private val currentDeckIdKey = longPreferencesKey("current_deck_id")
    private val cardContrastKey = intPreferencesKey("card_contrast")
    private val cardBackgroundKey = intPreferencesKey("card_background")

    val newCardsPerDay: Flow<Int> = context.dataStore.data.map { it[newCardsPerDayKey] ?: 10 }
    val dailyReviewLimit: Flow<Int> = context.dataStore.data.map { it[dailyReviewLimitKey] ?: 100 }
    val streak: Flow<Int> = context.dataStore.data.map { it[streakKey] ?: 0 }
    val currentDeckId: Flow<Long?> = context.dataStore.data.map { it[currentDeckIdKey] }
    /** 0 = Normal, 100 = High contrast for practice card (slider 0..100). Legacy: stored 1 is treated as 100. */
    val cardContrast: Flow<Int> = context.dataStore.data.map { prefs ->
        when (val v = prefs[cardContrastKey]) {
            null -> 100
            1 -> 100
            else -> v.coerceIn(0, 100)
        }
    }

    suspend fun setNewCardsPerDay(value: Int) {
        context.dataStore.edit { it[newCardsPerDayKey] = value }
    }

    suspend fun setDailyReviewLimit(value: Int) {
        context.dataStore.edit { it[dailyReviewLimitKey] = value }
    }

    /** 0 = Theme, 1 = Light, 2 = Dark, 3–7 = Ocean/Sunset/Aurora/Mint/Slate */
    val cardBackground: Flow<Int> = context.dataStore.data.map { it[cardBackgroundKey]?.coerceIn(0, 7) ?: 0 }

    suspend fun setCardContrast(value: Int) {
        context.dataStore.edit { it[cardContrastKey] = value.coerceIn(0, 100) }
    }

    suspend fun setCardBackground(value: Int) {
        context.dataStore.edit { it[cardBackgroundKey] = value.coerceIn(0, 7) }
    }

    suspend fun recordReviewDay() {
        context.dataStore.edit { prefs ->
            val today = getStartOfDayMillis(System.currentTimeMillis())
            val last = prefs[lastReviewDayKey] ?: 0L
            val currentStreak = prefs[streakKey] ?: 0
            val newStreak = when {
                last == 0L -> 1
                today == last -> currentStreak
                today - last == 86400_000L -> currentStreak + 1
                else -> 1
            }
            prefs[streakKey] = newStreak
            prefs[lastReviewDayKey] = today
        }
    }

    suspend fun setCurrentDeckId(id: Long?) {
        context.dataStore.edit { prefs ->
            if (id != null) prefs[currentDeckIdKey] = id
            else prefs.remove(currentDeckIdKey)
        }
    }

    suspend fun isFirstLaunch(): Boolean {
        val prefs = context.dataStore.data.first()
        val first = prefs[firstLaunchKey]
        if (first == null) {
            context.dataStore.edit { it[firstLaunchKey] = System.currentTimeMillis() }
            return true
        }
        return false
    }

    companion object {
        fun getStartOfDayMillis(time: Long): Long {
            val cal = Calendar.getInstance().apply { timeInMillis = time }
            cal.set(Calendar.HOUR_OF_DAY, 0)
            cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.SECOND, 0)
            cal.set(Calendar.MILLISECOND, 0)
            return cal.timeInMillis
        }
    }
}
