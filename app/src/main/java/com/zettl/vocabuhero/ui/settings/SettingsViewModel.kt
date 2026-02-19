package com.zettl.vocabuhero.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zettl.vocabuhero.data.prefs.SettingsStore
import com.zettl.vocabuhero.data.repository.CardRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SettingsUiState(
    val newCardsPerDay: Int = 10,
    val cardContrast: Int = 100,
    /** 0 = Theme, 1 = Light, 2 = Dark */
    val cardBackground: Int = 0
)

class SettingsViewModel(
    private val repository: CardRepository,
    private val settingsStore: SettingsStore
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            settingsStore.newCardsPerDay.collect { newLimit ->
                _uiState.update { it.copy(newCardsPerDay = newLimit) }
            }
        }
        viewModelScope.launch {
            settingsStore.cardContrast.collect { value ->
                _uiState.update { it.copy(cardContrast = value) }
            }
        }
        viewModelScope.launch {
            settingsStore.cardBackground.collect { value ->
                _uiState.update { it.copy(cardBackground = value) }
            }
        }
    }

    fun setNewCardsPerDay(value: Int) {
        _uiState.update { it.copy(newCardsPerDay = value) }
        viewModelScope.launch { settingsStore.setNewCardsPerDay(value) }
    }

    fun setCardContrast(value: Int) {
        _uiState.update { it.copy(cardContrast = value.coerceIn(0, 100)) }
    }

    fun persistCardContrast() {
        viewModelScope.launch { settingsStore.setCardContrast(_uiState.value.cardContrast) }
    }

    fun setCardBackground(value: Int) {
        _uiState.update { it.copy(cardBackground = value.coerceIn(0, 7)) }
        viewModelScope.launch { settingsStore.setCardBackground(value) }
    }
}
