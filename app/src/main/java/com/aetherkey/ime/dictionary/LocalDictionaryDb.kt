package com.aetherkey.ime.dictionary

import android.content.Context
import androidx.room.Database
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Entity(tableName = "words")
data class WordEntity(
    @PrimaryKey val word: String,
    val frequency: Int = 1,
    val lastUsed: Long = System.currentTimeMillis()
)

@Dao
interface WordDao {
    @Query("SELECT * FROM words ORDER BY frequency DESC LIMIT :limit")
    suspend fun getTopWords(limit: Int = 100): List<WordEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(word: WordEntity)

    @Query("UPDATE words SET frequency = frequency + 1, lastUsed = :now WHERE word = :word")
    suspend fun increment(word: String, now: Long = System.currentTimeMillis())
}

@Database(entities = [WordEntity::class], version = 1, exportSchema = false)
abstract class LocalDictionaryDb : RoomDatabase() {
    abstract fun wordDao(): WordDao

    companion object {
        @Volatile private var INSTANCE: LocalDictionaryDb? = null

        fun getInstance(context: Context): LocalDictionaryDb {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    LocalDictionaryDb::class.java,
                    "aether_dictionary.db"
                ).build().also { INSTANCE = it }
            }
        }
    }
}
