package com.zettl.vocabuhero.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS decks (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                name TEXT NOT NULL,
                frontLang TEXT NOT NULL,
                backLang TEXT NOT NULL,
                createdAt INTEGER NOT NULL
            )
        """.trimIndent())
        val now = System.currentTimeMillis()
        db.execSQL("INSERT INTO decks (id, name, frontLang, backLang, createdAt) VALUES (1, 'Italian – German', 'Italian', 'German', $now)")
        db.execSQL("ALTER TABLE cards ADD COLUMN deckId INTEGER NOT NULL DEFAULT 1")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_cards_deckId ON cards(deckId)")
    }
}

val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE cards ADD COLUMN article TEXT")
    }
}

val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE decks ADD COLUMN practiceReversed INTEGER NOT NULL DEFAULT 0")
    }
}

@Database(
    entities = [DeckEntity::class, CardEntity::class, ReviewLogEntity::class],
    version = 4,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun deckDao(): DeckDao
    abstract fun cardDao(): CardDao
    abstract fun reviewLogDao(): ReviewLogDao
}

object DatabaseProvider {
    @Volatile
    private var instance: AppDatabase? = null

    fun get(context: Context): AppDatabase {
        return instance ?: synchronized(this) {
            instance ?: Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "vocabuhero.db"
            ).addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4).build().also { instance = it }
        }
    }
}
