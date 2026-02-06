package com.zettl.vocabuhero.data.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "cards",
    indices = [Index("deckId")]
)
data class CardEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(defaultValue = "1")
    val deckId: Long,
    @ColumnInfo(name = "italian")
    val frontText: String,
    @ColumnInfo(name = "german")
    val backText: String,
    @ColumnInfo(defaultValue = "")
    val article: String? = null,
    val note: String? = null,
    val srsLevel: Int = 0,
    val lastReviewed: Long? = null,
    val nextReview: Long? = null,
    val easiness: Float? = null,
    val createdAt: Long = System.currentTimeMillis()
)
