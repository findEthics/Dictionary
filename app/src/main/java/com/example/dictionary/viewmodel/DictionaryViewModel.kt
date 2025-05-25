package com.example.dictionary.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.dictionary.data.entity.Word
import com.example.dictionary.data.repository.DictionaryRepository
import kotlinx.coroutines.flow.Flow

class DictionaryViewModel(private val repository: DictionaryRepository) : ViewModel() {

    val allWords: Flow<List<Word>> = repository.getAllWords()

    suspend fun searchWords(query: String): List<Word> {
        return repository.searchWords(query)
    }

    suspend fun getWordExact(word: String): Word? {
        return repository.getWordExact(word)
    }

    suspend fun insertWord(word: Word) {
        repository.insertWord(word)
    }

    suspend fun getWordCount(): Int {
        return repository.getWordCount()
    }
}

class DictionaryViewModelFactory(private val repository: DictionaryRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DictionaryViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return DictionaryViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
