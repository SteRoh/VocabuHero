package com.zettl.vocabuhero.ui.import_

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zettl.vocabuhero.data.csv.CsvParser
import com.zettl.vocabuhero.data.csv.RawImportRow
import com.zettl.vocabuhero.data.db.CardEntity
import com.zettl.vocabuhero.data.repository.CardRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ImportUiState(
    val rawText: String = "",
    val replaceNotAppend: Boolean = true,
    val frontColumn: Int = 0,
    val backColumn: Int = 1,
    val noteColumn: Int? = 2,
    val previewRows: List<RawImportRow> = emptyList(),
    val resultMessage: String? = null,
    val imported: Int = 0,
    val skipped: Int = 0
)

class ImportViewModel(private val repository: CardRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(ImportUiState())
    val uiState: StateFlow<ImportUiState> = _uiState.asStateFlow()

    fun setRawText(s: String) {
        _uiState.update { it.copy(rawText = s, resultMessage = null) }
        updatePreview()
    }

    fun setReplaceNotAppend(value: Boolean) { _uiState.update { it.copy(replaceNotAppend = value) } }
    fun setFrontColumn(i: Int) { _uiState.update { it.copy(frontColumn = i) }; updatePreview() }
    fun setBackColumn(i: Int) { _uiState.update { it.copy(backColumn = i) }; updatePreview() }
    fun setNoteColumn(i: Int?) { _uiState.update { it.copy(noteColumn = i) }; updatePreview() }

    private fun updatePreview() {
        val state = _uiState.value
        if (state.rawText.isBlank()) {
            _uiState.update { it.copy(previewRows = emptyList()) }
            return
        }
        val rows = try {
            CsvParser.parseWithColumnMapping(
                state.rawText,
                state.frontColumn,
                state.backColumn,
                state.noteColumn
            ).take(10)
        } catch (_: Exception) {
            emptyList()
        }
        _uiState.update { it.copy(previewRows = rows) }
    }

    fun import(onDone: () -> Unit) {
        val state = _uiState.value
        val rows = try {
            CsvParser.parseWithColumnMapping(
                state.rawText,
                state.frontColumn,
                state.backColumn,
                state.noteColumn
            )
        } catch (e: Exception) {
            _uiState.update { it.copy(resultMessage = "Parse error: ${e.message}") }
            return
        }
        if (rows.isEmpty()) {
            _uiState.update { it.copy(resultMessage = "No valid rows to import.") }
            return
        }
        viewModelScope.launch {
            val deckId = repository.currentDeckId.first() ?: run {
                _uiState.update { it.copy(resultMessage = "No deck selected. Open Deck and set a current deck.") }
                return@launch
            }
            val existing = repository.getAllCardsOnce(deckId)
            val existingFrontTexts = existing.map { it.frontText }.toSet()
            if (state.replaceNotAppend) {
                val entities = rows.map { r -> CardEntity(deckId = deckId, frontText = r.frontText, backText = r.backText, note = r.note) }
                repository.replaceAllCardsInDeck(deckId, entities)
                _uiState.update { it.copy(resultMessage = "Replaced deck with ${entities.size} cards.", imported = entities.size, skipped = 0) }
            } else {
                val toInsert = rows.filter { !existingFrontTexts.contains(it.frontText) }.map { r ->
                    CardEntity(deckId = deckId, frontText = r.frontText, backText = r.backText, note = r.note)
                }
                repository.insertCards(toInsert)
                _uiState.update { it.copy(resultMessage = "Imported ${toInsert.size}, skipped ${rows.size - toInsert.size} duplicates.", imported = toInsert.size, skipped = rows.size - toInsert.size) }
            }
            onDone()
        }
    }

    fun clearResult() { _uiState.update { it.copy(resultMessage = null) } }
}
