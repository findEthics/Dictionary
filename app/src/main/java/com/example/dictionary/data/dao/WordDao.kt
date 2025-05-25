// app/src/main/java/com/example/dictionary/data/dao/WordDao.kt
package com.example.dictionary.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.dictionary.data.entity.Word
import kotlinx.coroutines.flow.Flow

@Dao
interface WordDao {

    @Query("SELECT * FROM words WHERE word LIKE :searchWord ORDER BY word ASC LIMIT 20")
    suspend fun searchWords(searchWord: String): List<Word>

    @Query("SELECT * FROM words WHERE word = :exactWord LIMIT 1")
    suspend fun getWordExact(exactWord: String): Word?

    @Query("SELECT * FROM words WHERE word LIKE :prefix || '%' ORDER BY word ASC LIMIT 20")
    suspend fun searchWordsStartingWith(prefix: String): List<Word>

    @Query("SELECT * FROM words WHERE part_of_speech = :partOfSpeech ORDER BY word ASC LIMIT 50")
    suspend fun getWordsByPartOfSpeech(partOfSpeech: String): List<Word>

    @Query("SELECT DISTINCT part_of_speech FROM words WHERE part_of_speech IS NOT NULL ORDER BY part_of_speech ASC")
    suspend fun getAllPartsOfSpeech(): List<String>

    @Query("SELECT * FROM words ORDER BY word ASC")
    fun getAllWords(): Flow<List<Word>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWord(word: Word)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertWords(words: List<Word>)

    @Query("DELETE FROM words")
    suspend fun deleteAllWords()

    @Query("SELECT COUNT(*) FROM words")
    suspend fun getWordCount(): Int

    @Query("""
        SELECT * FROM words 
        WHERE word LIKE '%' || :searchTerm || '%' 
        ORDER BY 
            CASE 
                WHEN word = :searchTerm THEN 1
                WHEN word LIKE :searchTerm || '%' THEN 2 
                WHEN word LIKE '%' || :searchTerm THEN 3 
                ELSE 4 
            END, 
            word ASC 
        LIMIT 20
    """)
    suspend fun smartSearch(searchTerm: String): List<Word>

}
