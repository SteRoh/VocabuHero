package com.zettl.vocabuhero.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface ReviewLogDao {
    @Insert
    suspend fun insert(log: ReviewLogEntity)

    @Query("SELECT COUNT(*) FROM review_log WHERE rating >= 2")
    suspend fun countGoodOrEasy(): Int

    @Query("SELECT COUNT(*) FROM review_log")
    suspend fun countTotalReviews(): Int

    @Query("SELECT AVG(responseTimeMs) FROM review_log WHERE responseTimeMs > 0")
    suspend fun averageResponseTimeMs(): Double?

    @Query("SELECT * FROM review_log ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getRecent(limit: Int): List<ReviewLogEntity>

    @Query("SELECT COUNT(*) FROM review_log WHERE rating >= 2 AND cardId IN (:cardIds)")
    suspend fun countGoodOrEasyForCards(cardIds: List<Long>): Int

    @Query("SELECT COUNT(*) FROM review_log WHERE cardId IN (:cardIds)")
    suspend fun countTotalReviewsForCards(cardIds: List<Long>): Int

    @Query("SELECT AVG(responseTimeMs) FROM review_log WHERE responseTimeMs > 0 AND cardId IN (:cardIds)")
    suspend fun averageResponseTimeMsForCards(cardIds: List<Long>): Double?

    @Query("DELETE FROM review_log WHERE cardId IN (:cardIds)")
    suspend fun deleteByCardIds(cardIds: List<Long>)
}
