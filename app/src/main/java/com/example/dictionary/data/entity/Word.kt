package com.example.dictionary.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "words")
data class Word(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    @ColumnInfo(name = "word")
    val word: String,

    @ColumnInfo(name = "definition")
    val definition: String,

    @ColumnInfo(name = "part_of_speech")
    val partOfSpeech: String? = null,

    @ColumnInfo(name = "pronunciation")
    val pronunciation: String? = null,

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis()
)
