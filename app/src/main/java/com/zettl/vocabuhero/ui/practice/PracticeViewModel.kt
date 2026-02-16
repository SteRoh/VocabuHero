package com.zettl.vocabuhero.ui.practice

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zettl.vocabuhero.data.db.CardEntity
import com.zettl.vocabuhero.data.prefs.SettingsStore
import com.zettl.vocabuhero.data.repository.CardRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PracticeUiState(
    val cards: List<CardEntity> = emptyList(),
    val currentIndex: Int = 0,
    val isRevealed: Boolean = false,
    val revealTime: Long = 0L,
    val sessionComplete: Boolean = false,
    val isLoading: Boolean = true,
    /** 0f = normal, 1f = high contrast; used to interpolate card colors */
    val cardContrastLevel: Float = 1f,
    /** 0 = Theme, 1 = Light, 2 = Dark card background */
    val cardBackgroundPreset: Int = 0,
    val reversed: Boolean = false
)

class PracticeViewModel(
    private val repository: CardRepository,
    private val settingsStore: SettingsStore
) : ViewModel() {

    private val _uiState = MutableStateFlow(PracticeUiState())
    val uiState: StateFlow<PracticeUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            settingsStore.cardContrast.collect { value ->
                _uiState.update { it.copy(cardContrastLevel = value / 100f) }
            }
        }
        viewModelScope.launch {
            settingsStore.cardBackground.collect { value ->
                _uiState.update { it.copy(cardBackgroundPreset = value) }
            }
        }
    }

    fun loadSession() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val deckId = repository.currentDeckId.first()
            val deck = if (deckId != null) repository.getDeck(deckId) else null
            val list = if (deckId != null) repository.getSessionCards(deckId) else emptyList()
            _uiState.update {
                it.copy(
                    cards = list,
                    currentIndex = 0,
                    isRevealed = false,
                    sessionComplete = list.isEmpty(),
                    isLoading = false,
                    reversed = deck?.practiceReversed ?: false
                )
            }
        }
    }

    fun reveal() {
        _uiState.update {
            it.copy(isRevealed = true, revealTime = System.currentTimeMillis())
        }
    }

    fun rate(rating: Int) {
        val state = _uiState.value
        val card = state.cards.getOrNull(state.currentIndex) ?: return
        viewModelScope.launch {
            val responseTime = if (state.isRevealed) System.currentTimeMillis() - state.revealTime else 0L
            repository.recordReview(card, rating, responseTime)
        }
        val nextIndex = state.currentIndex + 1
        _uiState.update {
            it.copy(
                currentIndex = nextIndex,
                isRevealed = false,
                sessionComplete = nextIndex >= state.cards.size
            )
        }
    }

    fun currentCard(): CardEntity? = _uiState.value.cards.getOrNull(_uiState.value.currentIndex)
}
