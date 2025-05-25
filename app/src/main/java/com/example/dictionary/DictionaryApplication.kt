package com.example.dictionary

import android.app.Application
import com.example.dictionary.data.database.DictionaryDatabase
import com.example.dictionary.data.repository.DictionaryRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class DictionaryApplication : Application() {
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    val database by lazy {
        DictionaryDatabase.getDatabase(this, applicationScope)
    }

    val repository by lazy {
        DictionaryRepository(database.wordDao())
    }

    override fun onCreate() {
        super.onCreate()
        // Force database initialization
        applicationScope.launch {
            // Access DAO to trigger database creation
            database.wordDao().getWordCount()
        }
    }

    override fun onTerminate() {
        applicationScope.cancel()
        super.onTerminate()
    }
}
