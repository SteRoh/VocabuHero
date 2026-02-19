package com.zettl.vocabuhero.data.db

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/** Result of COUNT GROUP BY srsLevel for SRS bucket visualization. */
data class SrsLevelCount(val srsLevel: Int, val count: Int)

@Dao
interface CardDao {
    @Query("SELECT * FROM cards WHERE deckId = :deckId ORDER BY createdAt DESC")
    fun getAll(deckId: Long): Flow<List<CardEntity>>

    @Query("SELECT * FROM cards WHERE id = :id")
    suspend fun getById(id: Long): CardEntity?

    @Query("SELECT * FROM cards WHERE deckId = :deckId AND nextReview IS NULL ORDER BY createdAt ASC LIMIT :limit")
    suspend fun getNew(deckId: Long, limit: Int): List<CardEntity>

    @Query("SELECT * FROM cards WHERE deckId = :deckId AND nextReview IS NOT NULL AND nextReview <= :before ORDER BY nextReview ASC")
    suspend fun getDue(deckId: Long, before: Long): List<CardEntity>

    @Query("SELECT COUNT(*) FROM cards WHERE deckId = :deckId")
    fun count(deckId: Long): Flow<Int>

    @Query("SELECT COUNT(*) FROM cards WHERE deckId = :deckId")
    suspend fun countOnce(deckId: Long): Int

    /** Emits whenever the cards table changes (add/update/delete). Used to refresh deck list counts. */
    @Query("SELECT COUNT(*) FROM cards")
    fun totalCardCountFlow(): Flow<Int>

    @Query("SELECT COUNT(*) FROM cards WHERE deckId = :deckId AND nextReview IS NULL")
    suspend fun countNew(deckId: Long): Int

    @Query("SELECT COUNT(*) FROM cards WHERE deckId = :deckId AND nextReview IS NOT NULL AND nextReview >= :dayStart AND nextReview <= :dayEnd")
    suspend fun countDue(deckId: Long, dayStart: Long, dayEnd: Long): Int

    @Query("SELECT COUNT(*) FROM cards WHERE deckId = :deckId AND srsLevel > 0 AND lastReviewed IS NOT NULL")
    suspend fun countLearned(deckId: Long): Int

    @Query("SELECT id FROM cards WHERE deckId = :deckId")
    suspend fun getCardIdsByDeck(deckId: Long): List<Long>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(card: CardEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(cards: List<CardEntity>)

    @Update
    suspend fun update(card: CardEntity)

    @Delete
    suspend fun delete(card: CardEntity)

    @Query("DELETE FROM cards WHERE deckId = :deckId")
    suspend fun deleteAllInDeck(deckId: Long)

    @Query("UPDATE cards SET srsLevel = 0, lastReviewed = NULL, nextReview = NULL, easiness = NULL WHERE deckId = :deckId")
    suspend fun resetProgressInDeck(deckId: Long)

    @Query("SELECT COUNT(*) FROM cards WHERE deckId = :deckId AND srsLevel = 0 AND nextReview IS NOT NULL")
    suspend fun countLearning(deckId: Long): Int

    @Query("SELECT srsLevel, COUNT(*) as count FROM cards WHERE deckId = :deckId AND srsLevel > 0 GROUP BY srsLevel ORDER BY srsLevel")
    suspend fun getSrsLevelCounts(deckId: Long): List<SrsLevelCount>
}
