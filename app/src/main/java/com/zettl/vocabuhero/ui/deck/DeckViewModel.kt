package com.zettl.vocabuhero.ui.deck

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zettl.vocabuhero.data.db.CardEntity
import com.zettl.vocabuhero.data.db.DeckEntity
import com.zettl.vocabuhero.data.repository.CardRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class DeckUiState(
    val deck: DeckEntity? = null,
    val cards: List<CardEntity> = emptyList(),
    val isCurrentDeck: Boolean = false,
    val showResetProgressDialog: Boolean = false,
    val navigateToAddCard: Boolean = false,
    val cardToDelete: CardEntity? = null
)

class DeckViewModel(
    private val repository: CardRepository,
    val deckId: Long
) : ViewModel() {

    private val _uiState = MutableStateFlow(DeckUiState())
    val uiState: StateFlow<DeckUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.getDeck(deckId)?.let { deck ->
                _uiState.update { it.copy(deck = deck) }
            }
        }
        viewModelScope.launch {
            repository.currentDeckId.collect { currentId ->
                _uiState.update { it.copy(isCurrentDeck = currentId == deckId) }
            }
        }
        viewModelScope.launch {
            if (deckId != 0L) {
                repository.getAllCards(deckId).collect { list ->
                    _uiState.update { it.copy(cards = list) }
                }
            }
        }
    }

    fun setAsCurrentDeck() {
        viewModelScope.launch {
            repository.setCurrentDeckId(deckId)
        }
    }

    fun showResetProgressDialog(show: Boolean) {
        _uiState.update { it.copy(showResetProgressDialog = show) }
    }

    fun resetProgress() {
        viewModelScope.launch {
            repository.resetDeckProgress(deckId)
            _uiState.update { it.copy(showResetProgressDialog = false) }
        }
    }

    /** Sets this deck as current and signals to navigate to add-card screen. */
    fun onAddCardClick() {
        viewModelScope.launch {
            repository.setCurrentDeckId(deckId)
            _uiState.update { it.copy(navigateToAddCard = true) }
        }
    }

    fun clearNavigateToAddCard() {
        _uiState.update { it.copy(navigateToAddCard = false) }
    }

    fun showDeleteCardDialog(card: CardEntity?) {
        _uiState.update { it.copy(cardToDelete = card) }
    }

    fun deleteCard(card: CardEntity) {
        viewModelScope.launch {
            repository.deleteCard(card)
            _uiState.update { it.copy(cardToDelete = null) }
        }
    }
}
