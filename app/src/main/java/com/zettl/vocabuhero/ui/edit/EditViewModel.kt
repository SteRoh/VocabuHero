package com.zettl.vocabuhero.ui.edit

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zettl.vocabuhero.data.db.CardEntity
import com.zettl.vocabuhero.data.repository.CardRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class EditUiState(
    val frontText: String = "",
    val backText: String = "",
    val note: String = "",
    val cardId: Long = 0L,
    val saved: Boolean = false,
    val frontLabel: String = "Front",
    val backLabel: String = "Back"
)

class EditViewModel(
    private val repository: CardRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val cardId: Long? = savedStateHandle.get<String>("cardId")?.let { s ->
        if (s == "new") null else s.toLongOrNull()?.takeIf { it > 0 }
    }

    private val _uiState = MutableStateFlow(EditUiState())
    val uiState: StateFlow<EditUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.currentDeckId.first()?.let { deckId ->
                repository.getDeck(deckId)?.let { deck ->
                    _uiState.update {
                        it.copy(frontLabel = deck.frontLang, backLabel = deck.backLang)
                    }
                }
            }
        }
        if (cardId != null) {
            viewModelScope.launch {
                repository.getCard(cardId)?.let { card ->
                    val deck = repository.getDeck(card.deckId)
                    _uiState.update {
                        it.copy(
                            frontText = card.frontText,
                            backText = card.backText,
                            note = card.note ?: "",
                            cardId = card.id,
                            frontLabel = deck?.frontLang ?: it.frontLabel,
                            backLabel = deck?.backLang ?: it.backLabel
                        )
                    }
                }
            }
        }
    }

    fun setFrontText(s: String) { _uiState.update { it.copy(frontText = s) } }
    fun setBackText(s: String) { _uiState.update { it.copy(backText = s) } }
    fun setNote(s: String) { _uiState.update { it.copy(note = s) } }

    fun save(onSaved: () -> Unit) {
        val state = _uiState.value
        if (state.frontText.isBlank() || state.backText.isBlank()) return
        viewModelScope.launch {
            val deckId = repository.currentDeckId.first() ?: return@launch
            val entity = CardEntity(
                id = state.cardId,
                deckId = deckId,
                frontText = state.frontText.trim(),
                backText = state.backText.trim(),
                note = state.note.trim().takeIf { it.isNotBlank() }
            )
            repository.saveCard(entity)
            _uiState.update { it.copy(saved = true) }
            onSaved()
        }
    }

    fun delete(onDeleted: () -> Unit) {
        val state = _uiState.value
        if (state.cardId == 0L) return
        viewModelScope.launch {
            repository.getCard(state.cardId)?.let { repository.deleteCard(it) }
            onDeleted()
        }
    }
}
