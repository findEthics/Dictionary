package com.example.dictionary.data.repository

import com.example.dictionary.data.dao.WordDao
import com.example.dictionary.data.entity.Word
import kotlinx.coroutines.flow.Flow

class DictionaryRepository(private val wordDao: WordDao) {

    fun getAllWords(): Flow<List<Word>> = wordDao.getAllWords()

    suspend fun searchWords(query: String): List<Word> {
        return wordDao.searchWords("%$query%")
    }

    suspend fun getWordExact(word: String): Word? {
        return wordDao.getWordExact(word.lowercase())
    }

    suspend fun insertWord(word: Word) {
        wordDao.insertWord(word)
    }

    suspend fun insertWords(words: List<Word>) {
        wordDao.insertWords(words)
    }

    suspend fun getWordCount(): Int {
        return wordDao.getWordCount()
    }
}
