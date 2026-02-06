package com.zettl.vocabuhero.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "decks")
data class DeckEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val frontLang: String,
    val backLang: String,
    val createdAt: Long = System.currentTimeMillis()
)
