package com.zettl.vocabuhero.data.repository

import android.content.Context
import com.zettl.vocabuhero.data.csv.CsvParser
import com.zettl.vocabuhero.data.db.CardEntity
import com.zettl.vocabuhero.data.db.CardDao
import com.zettl.vocabuhero.data.db.SrsLevelCount
import com.zettl.vocabuhero.data.db.DeckDao
import com.zettl.vocabuhero.data.db.DeckEntity
import com.zettl.vocabuhero.data.db.ReviewLogDao
import com.zettl.vocabuhero.data.db.ReviewLogEntity
import com.zettl.vocabuhero.data.prefs.SettingsStore
import com.zettl.vocabuhero.data.srs.SrsScheduler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

class CardRepository(
    private val deckDao: DeckDao,
    private val cardDao: CardDao,
    private val reviewLogDao: ReviewLogDao,
    private val settingsStore: SettingsStore
) {

    val streak: Flow<Int> = settingsStore.streak
    val newCardsPerDay: Flow<Int> = settingsStore.newCardsPerDay
    val dailyReviewLimit: Flow<Int> = settingsStore.dailyReviewLimit
    val currentDeckId: Flow<Long?> = settingsStore.currentDeckId

    fun getDecks(): Flow<List<DeckEntity>> = deckDao.getAll()

    suspend fun getDecksOnce(): List<DeckEntity> = withContext(Dispatchers.IO) {
        deckDao.getAllOnce()
    }

    suspend fun getDeck(id: Long): DeckEntity? = withContext(Dispatchers.IO) {
        deckDao.getById(id)
    }

    suspend fun createDeck(name: String, frontLang: String, backLang: String): Long = withContext(Dispatchers.IO) {
        deckDao.insert(DeckEntity(name = name, frontLang = frontLang, backLang = backLang))
    }

    suspend fun updateDeck(deck: DeckEntity) = withContext(Dispatchers.IO) {
        deckDao.update(deck)
    }

    suspend fun setCurrentDeckId(id: Long?) = withContext(Dispatchers.IO) {
        settingsStore.setCurrentDeckId(id)
    }

    fun totalCount(deckId: Long): Flow<Int> = cardDao.count(deckId)

    /** Emits whenever any card is added, updated, or deleted. Use in combine to refresh deck list counts. */
    fun totalCardCountFlow(): Flow<Int> = cardDao.totalCardCountFlow()

    suspend fun getTotalCountOnce(deckId: Long): Int = withContext(Dispatchers.IO) {
        cardDao.countOnce(deckId)
    }

    suspend fun getSessionCards(deckId: Long): List<CardEntity> = withContext(Dispatchers.IO) {
        val newLimit = settingsStore.newCardsPerDay.first()
        val reviewLimit = settingsStore.dailyReviewLimit.first()
        val dayEnd = SettingsStore.getStartOfDayMillis(System.currentTimeMillis()) + 86400_000L - 1
        val due = cardDao.getDue(deckId, dayEnd)
        val dueTake = due.take(reviewLimit)
        val remainingSlots = (reviewLimit - dueTake.size).coerceAtLeast(0)
        val newTake = cardDao.getNew(deckId, (newLimit).coerceAtMost(remainingSlots))
        (dueTake + newTake).shuffled()
    }

    suspend fun getCard(id: Long): CardEntity? = withContext(Dispatchers.IO) {
        cardDao.getById(id)
    }

    fun getAllCards(deckId: Long): Flow<List<CardEntity>> = cardDao.getAll(deckId)

    suspend fun getAllCardsOnce(deckId: Long): List<CardEntity> = withContext(Dispatchers.IO) {
        cardDao.getAll(deckId).first()
    }

    suspend fun saveCard(card: CardEntity) = withContext(Dispatchers.IO) {
        if (card.id == 0L) cardDao.insert(card) else cardDao.update(card)
    }

    suspend fun deleteCard(card: CardEntity) = withContext(Dispatchers.IO) {
        cardDao.delete(card)
    }

    suspend fun recordReview(card: CardEntity, rating: Int, responseTimeMs: Long) = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()
        val updated = SrsScheduler.nextState(card, rating, now)
        cardDao.update(updated)
        reviewLogDao.insert(ReviewLogEntity(cardId = card.id, rating = rating, responseTimeMs = responseTimeMs))
        settingsStore.recordReviewDay()
    }

    suspend fun insertCards(cards: List<CardEntity>) = withContext(Dispatchers.IO) {
        cardDao.insertAll(cards)
    }

    suspend fun replaceAllCardsInDeck(deckId: Long, cards: List<CardEntity>) = withContext(Dispatchers.IO) {
        cardDao.deleteAllInDeck(deckId)
        cardDao.insertAll(cards)
    }

    suspend fun countDue(deckId: Long, dayStart: Long, dayEnd: Long): Int = withContext(Dispatchers.IO) {
        cardDao.countDue(deckId, dayStart, dayEnd)
    }

    suspend fun countNew(deckId: Long): Int = withContext(Dispatchers.IO) {
        cardDao.countNew(deckId)
    }

    suspend fun countLearned(deckId: Long): Int = withContext(Dispatchers.IO) {
        cardDao.countLearned(deckId)
    }

    /** SRS bucket labels by level: Good path 1→3→7→30d, Easy 1→7→30d. Level 0 split into New/Learning. */
    suspend fun getSrsBuckets(deckId: Long): List<Pair<String, Int>> = withContext(Dispatchers.IO) {
        val new = cardDao.countNew(deckId)
        val learning = cardDao.countLearning(deckId)
        val levelCounts = cardDao.getSrsLevelCounts(deckId).associate { it.srsLevel to it.count }
        val labels = mapOf(1 to "1d", 2 to "3d", 3 to "7d", 4 to "30d")
        buildList {
            add("New" to new)
            add("Learning" to learning)
            for (level in 1..4) {
                val count = levelCounts[level] ?: 0
                add(labels[level]!! to count)
            }
        }
    }

    suspend fun isFirstLaunch(): Boolean = settingsStore.isFirstLaunch()

    /** If there are decks but no current deck selected, set current to the first deck. */
    suspend fun ensureCurrentDeck() = withContext(Dispatchers.IO) {
        val current = settingsStore.currentDeckId.first()
        if (current != null) return@withContext
        val decks = deckDao.getAllOnce()
        if (decks.isNotEmpty()) settingsStore.setCurrentDeckId(decks.first().id)
    }

    private suspend fun cardIdsForDeck(deckId: Long): List<Long> {
        val ids = cardDao.getCardIdsByDeck(deckId)
        return if (ids.isEmpty()) listOf(-1L) else ids
    }

    suspend fun getAccuracyPercent(deckId: Long): Float = withContext(Dispatchers.IO) {
        val ids = cardIdsForDeck(deckId)
        val total = reviewLogDao.countTotalReviewsForCards(ids)
        if (total == 0) 0f else reviewLogDao.countGoodOrEasyForCards(ids) * 100f / total
    }

    suspend fun getAverageResponseTimeMs(deckId: Long): Long? = withContext(Dispatchers.IO) {
        val ids = cardIdsForDeck(deckId)
        reviewLogDao.averageResponseTimeMsForCards(ids)?.toLong()
    }

    /** Resets SRS progress for all cards in the deck and deletes their review history. */
    suspend fun resetDeckProgress(deckId: Long) = withContext(Dispatchers.IO) {
        val cardIds = cardDao.getCardIdsByDeck(deckId)
        if (cardIds.isNotEmpty()) {
            reviewLogDao.deleteByCardIds(cardIds)
        }
        cardDao.resetProgressInDeck(deckId)
    }

    /** Ensures the built-in decks exist and loads their CSV from assets if newly created. */
    suspend fun ensureBuiltInDecks(context: Context) = withContext(Dispatchers.IO) {
        val builtIns = listOf(
            Triple("English – German BASIC", "English", "German"),
            Triple("English – German ADVANCED", "English", "German"),
            Triple("English – German EXPERT", "English", "German"),
            Triple("Italian – German BASIC", "Italian", "German"),
            Triple("Italian – German ADVANCED", "Italian", "German"),
            Triple("Italian – German EXPERT", "Italian", "German"),
            Triple("French – German BASIC", "French", "German"),
            Triple("French – German ADVANCED", "French", "German"),
            Triple("French – German EXPERT", "French", "German"),
            Triple("Spanish – German BASIC", "Spanish", "German"),
            Triple("Spanish – German ADVANCED", "Spanish", "German"),
            Triple("Spanish – German EXPERT", "Spanish", "German"),
            Triple("GFK", "English", "German")
        )
        val assetNames = mapOf(
            "English – German BASIC" to "english_german_basic.csv",
            "English – German ADVANCED" to "english_german_advanced.csv",
            "English – German EXPERT" to "english_german_expert.csv",
            "Italian – German BASIC" to "italian_german_basic.csv",
            "Italian – German ADVANCED" to "italian_german_advanced.csv",
            "Italian – German EXPERT" to "italian_german_expert.csv",
            "French – German BASIC" to "french_german_basic.csv",
            "French – German ADVANCED" to "french_german_advanced.csv",
            "French – German EXPERT" to "french_german_expert.csv",
            "Spanish – German BASIC" to "spanish_german_basic.csv",
            "Spanish – German ADVANCED" to "spanish_german_advanced.csv",
            "Spanish – German EXPERT" to "spanish_german_expert.csv",
            "GFK" to "gfk.csv"
        )
        val existing = deckDao.getAllOnce()
        for ((name, frontLang, backLang) in builtIns) {
            if (existing.any { it.name == name }) continue
            val deckId = deckDao.insert(DeckEntity(name = name, frontLang = frontLang, backLang = backLang))
            val asset = assetNames[name] ?: continue
            try {
                context.assets.open(asset).use { stream ->
                    val rows = CsvParser.parse(stream)
                    val entities = rows.map { r ->
                        CardEntity(deckId = deckId, frontText = r.frontText, backText = r.backText, note = r.note)
                    }
                    if (entities.isNotEmpty()) cardDao.insertAll(entities)
                }
            } catch (_: Exception) { /* asset missing */ }
        }
        ensureCurrentDeck()
    }
}
