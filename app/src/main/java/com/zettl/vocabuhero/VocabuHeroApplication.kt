package com.zettl.vocabuhero

import android.app.Application
import com.zettl.vocabuhero.data.db.DatabaseProvider
import com.zettl.vocabuhero.data.prefs.SettingsStore
import com.zettl.vocabuhero.data.repository.CardRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class VocabuHeroApplication : Application() {
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    val database by lazy { DatabaseProvider.get(this) }
    val settingsStore by lazy { SettingsStore(this) }
    val cardRepository by lazy {
        CardRepository(
            deckDao = database.deckDao(),
            cardDao = database.cardDao(),
            reviewLogDao = database.reviewLogDao(),
            settingsStore = settingsStore
        )
    }

    override fun onCreate() {
        super.onCreate()
        applicationScope.launch {
            cardRepository.ensureBuiltInDecks(this@VocabuHeroApplication)
        }
    }
}
