package com.zettl.vocabuhero

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.zettl.vocabuhero.ui.deck.DeckListViewModel
import com.zettl.vocabuhero.ui.deck.DeckViewModel
import com.zettl.vocabuhero.ui.home.HomeViewModel
import com.zettl.vocabuhero.ui.import_.ImportViewModel
import com.zettl.vocabuhero.ui.practice.PracticeViewModel
import com.zettl.vocabuhero.ui.settings.SettingsViewModel
import com.zettl.vocabuhero.ui.stats.StatsViewModel

class ViewModelFactory(private val app: VocabuHeroApplication) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(HomeViewModel::class.java) -> HomeViewModel(app.cardRepository) as T
            modelClass.isAssignableFrom(PracticeViewModel::class.java) -> PracticeViewModel(app.cardRepository, app.settingsStore) as T
            modelClass.isAssignableFrom(ImportViewModel::class.java) -> ImportViewModel(app.cardRepository) as T
            modelClass.isAssignableFrom(StatsViewModel::class.java) -> StatsViewModel(app.cardRepository) as T
            modelClass.isAssignableFrom(SettingsViewModel::class.java) -> SettingsViewModel(app.cardRepository, app.settingsStore) as T
            modelClass.isAssignableFrom(DeckListViewModel::class.java) -> DeckListViewModel(app.cardRepository) as T
            else -> throw IllegalArgumentException("Unknown ViewModel ${modelClass.name}")
        }
    }
}
