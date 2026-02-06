package com.zettl.vocabuhero.ui.deck

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zettl.vocabuhero.data.db.DeckEntity
import com.zettl.vocabuhero.data.prefs.SettingsStore
import com.zettl.vocabuhero.data.repository.CardRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class DeckWithCounts(
    val deck: DeckEntity,
    val dueToday: Int,
    val newCount: Int,
    val total: Int
)

data class DeckListUiState(
    val decks: List<DeckWithCounts> = emptyList(),
    val currentDeckId: Long? = null,
    val showAddDeckDialog: Boolean = false
)

class DeckListViewModel(private val repository: CardRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(DeckListUiState())
    val uiState: StateFlow<DeckListUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                repository.getDecks(),
                repository.currentDeckId,
                repository.totalCardCountFlow()
            ) { deckList, currentId, _ ->
                deckList to currentId
            }.collect { (deckList, currentId) ->
                val dayStart = SettingsStore.getStartOfDayMillis(System.currentTimeMillis())
                val dayEnd = dayStart + 86400_000L - 1
                val withCounts = deckList.map { deck ->
                    DeckWithCounts(
                        deck = deck,
                        dueToday = repository.countDue(deck.id, dayStart, dayEnd),
                        newCount = repository.countNew(deck.id),
                        total = repository.getTotalCountOnce(deck.id)
                    )
                }
                _uiState.update {
                    it.copy(
                        decks = withCounts,
                        currentDeckId = currentId
                    )
                }
            }
        }
    }

    fun refreshCounts() {
        viewModelScope.launch {
            val dayStart = SettingsStore.getStartOfDayMillis(System.currentTimeMillis())
            val dayEnd = dayStart + 86400_000L - 1
            val deckList = repository.getDecksOnce()
            val withCounts = deckList.map { deck ->
                DeckWithCounts(
                    deck = deck,
                    dueToday = repository.countDue(deck.id, dayStart, dayEnd),
                    newCount = repository.countNew(deck.id),
                    total = repository.getTotalCountOnce(deck.id)
                )
            }
            _uiState.update {
                it.copy(decks = withCounts)
            }
        }
    }

    fun setCurrentDeck(id: Long) {
        viewModelScope.launch {
            repository.setCurrentDeckId(id)
        }
    }

    fun showAddDeckDialog(show: Boolean) {
        _uiState.update { it.copy(showAddDeckDialog = show) }
    }

    fun createDeck(name: String, frontLang: String, backLang: String) {
        if (name.isBlank() || frontLang.isBlank() || backLang.isBlank()) return
        viewModelScope.launch {
            repository.createDeck(name.trim(), frontLang.trim(), backLang.trim())
            _uiState.update { it.copy(showAddDeckDialog = false) }
        }
    }
}
