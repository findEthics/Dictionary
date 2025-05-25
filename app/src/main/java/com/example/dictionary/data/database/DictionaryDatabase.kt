// app/src/main/java/com/example/dictionary/data/database/DictionaryDatabase.kt
package com.example.dictionary.data.database

import android.content.Context
import android.widget.Toast
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.dictionary.data.dao.WordDao
import com.example.dictionary.data.entity.Word
import com.example.dictionary.data.wordnet.WordNetParser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [Word::class],
    version = 1,
    exportSchema = false
)
abstract class DictionaryDatabase : RoomDatabase() {

    abstract fun wordDao(): WordDao

    companion object {
        @Volatile
        private var INSTANCE: DictionaryDatabase? = null

        fun getDatabase(
            context: Context,
            scope: CoroutineScope
        ): DictionaryDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    DictionaryDatabase::class.java,
                    "dictionary_database"
                )
                    .addCallback(DictionaryDatabaseCallback(context, scope))
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DictionaryDatabaseCallback(
            private val context: Context,
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {

            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                println("DictionaryDB: Populating database")
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {

                        populateDatabase(database.wordDao(), context)
                    }
                }
            }
        }

        suspend fun populateDatabase(wordDao: WordDao, context: Context) {
            // Clear any existing data
            wordDao.deleteAllWords()
            println("DictionaryDB: Starting loading data")

            try {
                // Parse WordNet data
                val parser = WordNetParser(context)
                val wordNetWords = parser.parseWordNetData()

                if (wordNetWords.isNotEmpty()) {
                    // Insert WordNet data in batches for better performance
                    val batchSize = 500
                    wordNetWords.chunked(batchSize).forEach { batch ->
                        wordDao.insertWords(batch)
                    }
                    Toast.makeText(context, "Dictionary loaded with ${wordNetWords.size} words", Toast.LENGTH_SHORT).show()

                    android.util.Log.d("DictionaryDB", "Loaded ${wordNetWords.size} words from WordNet")
                } else {
                    Toast.makeText(context, "No WordNet data found, database may be empty", Toast.LENGTH_SHORT).show()
                    android.util.Log.w("DictionaryDB", "No WordNet data found, database may be empty")
                }

            } catch (e: Exception) {
                Toast.makeText(context, "Error populating database: ${e.message}", Toast.LENGTH_SHORT).show()
                android.util.Log.e("DictionaryDB", "Error populating database", e)
            }
        }
    }
}
