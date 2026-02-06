package com.zettl.vocabuhero.ui.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zettl.vocabuhero.data.prefs.SettingsStore
import com.zettl.vocabuhero.data.repository.CardRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class StatsUiState(
    val total: Int = 0,
    val learned: Int = 0,
    val dueToday: Int = 0,
    val accuracyPercent: Float = 0f,
    val streak: Int = 0,
    val avgResponseTimeMs: Long? = null,
    val retentionEstimate: Float = 0f
)

class StatsViewModel(private val repository: CardRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(StatsUiState())
    val uiState: StateFlow<StatsUiState> = _uiState.asStateFlow()

    init {
        loadStats()
    }

    fun loadStats() {
        viewModelScope.launch {
            val deckId = repository.currentDeckId.first() ?: return@launch
            val dayStart = SettingsStore.getStartOfDayMillis(System.currentTimeMillis())
            val dayEnd = dayStart + 86400_000L - 1
            val total = repository.getTotalCountOnce(deckId)
            val learned = repository.countLearned(deckId)
            val due = repository.countDue(deckId, dayStart, dayEnd)
            val accuracy = repository.getAccuracyPercent(deckId)
            val streak = repository.streak.first()
            val avgTime = repository.getAverageResponseTimeMs(deckId)
            val retention = if (total > 0) learned * 100f / total else 0f
            _uiState.update {
                it.copy(
                    total = total,
                    learned = learned,
                    dueToday = due,
                    accuracyPercent = accuracy,
                    streak = streak,
                    avgResponseTimeMs = avgTime,
                    retentionEstimate = retention
                )
            }
        }
    }
}
