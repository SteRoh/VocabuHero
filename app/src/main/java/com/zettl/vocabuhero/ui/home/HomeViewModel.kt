package com.zettl.vocabuhero.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zettl.vocabuhero.data.prefs.SettingsStore
import com.zettl.vocabuhero.data.repository.CardRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HomeUiState(
    val total: Int = 0,
    val dueToday: Int = 0,
    val learned: Int = 0,
    val streak: Int = 0,
    val currentDeckId: Long? = null,
    val currentDeckName: String? = null
)

class HomeViewModel(private val repository: CardRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.streak.collect { streak ->
                _uiState.update { it.copy(streak = streak) }
            }
        }
        viewModelScope.launch {
            combine(
                repository.currentDeckId,
                repository.getDecks()
            ) { currentId, deckList ->
                val deck = deckList.find { it.id == currentId }
                currentId to deck?.name
            }.collect { (currentId, deckName) ->
                _uiState.update {
                    it.copy(currentDeckId = currentId, currentDeckName = deckName)
                }
                refreshCounts()
            }
        }
    }

    fun refreshCounts() {
        viewModelScope.launch {
            val deckId = _uiState.value.currentDeckId ?: return@launch
            val dayStart = SettingsStore.getStartOfDayMillis(System.currentTimeMillis())
            val dayEnd = dayStart + 86400_000L - 1
            _uiState.update {
                it.copy(
                    total = repository.getTotalCountOnce(deckId),
                    dueToday = repository.countDue(deckId, dayStart, dayEnd),
                    learned = repository.countLearned(deckId)
                )
            }
        }
    }
}
