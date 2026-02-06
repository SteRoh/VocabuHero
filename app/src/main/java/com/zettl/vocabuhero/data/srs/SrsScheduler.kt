package com.zettl.vocabuhero.data.srs

import com.zettl.vocabuhero.data.db.CardEntity

/**
 * Simple interval SRS: Again (reset), Hard (1d), Good (1→3→7→30d), Easy (1→7→30d).
 * Rating: 0=Again, 1=Hard, 2=Good, 3=Easy.
 */
object SrsScheduler {
    private val goodIntervals = listOf(1, 3, 7, 30) // days
    private val easyIntervals = listOf(1, 7, 30)

    fun nextState(card: CardEntity, rating: Int, now: Long): CardEntity {
        return when (rating) {
            0 -> card.copy(
                srsLevel = 0,
                lastReviewed = now,
                nextReview = now + 60_000L // 1 min for same session
            )
            1 -> card.copy(
                srsLevel = (card.srsLevel + 1).coerceAtMost(goodIntervals.size),
                lastReviewed = now,
                nextReview = now + 1 * millisPerDay
            )
            2 -> {
                val level = (card.srsLevel + 1).coerceAtMost(goodIntervals.size)
                val days = goodIntervals[(level - 1).coerceIn(0, goodIntervals.size - 1)]
                card.copy(
                    srsLevel = level,
                    lastReviewed = now,
                    nextReview = now + days * millisPerDay
                )
            }
            3 -> {
                val level = (card.srsLevel + 1).coerceAtMost(easyIntervals.size)
                val days = easyIntervals[(level - 1).coerceIn(0, easyIntervals.size - 1)]
                card.copy(
                    srsLevel = level,
                    lastReviewed = now,
                    nextReview = now + days * millisPerDay
                )
            }
            else -> card
        }
    }

    private const val millisPerDay = 24 * 60 * 60 * 1000L
}
