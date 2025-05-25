// app/src/main/java/com/example/dictionary/data/wordnet/WordNetParser.kt
package com.example.dictionary.data.wordnet

import android.content.Context
import android.util.Log
import com.example.dictionary.data.entity.Word
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader

class WordNetParser(private val context: Context) {

    private val partsOfSpeech = listOf("noun", "verb", "adj", "adv")

    suspend fun parseWordNetData(): List<Word> = withContext(Dispatchers.IO) {
        val words = mutableListOf<Word>()

        try {
            partsOfSpeech.forEach { pos ->
                val assetPath = "wordnet/data.${pos}"
                Log.d("WordNetParser", "Processing POS: $pos at $assetPath")

                context.assets.open(assetPath).use { stream ->
                    BufferedReader(InputStreamReader(stream)).use { reader ->
                        var line: String?
                        while (reader.readLine().also { line = it } != null) {
                            if (line?.startsWith("  ") == false) {
                                parseSynsetLine(line, pos)?.let { words.addAll(it) }
                            }
                        }
                    }
                }
            }
            Log.d("WordNetParser", "Successfully parsed ${words.size} words")
        } catch (e: Exception) {
            Log.e("WordNetParser", "Error parsing WordNet", e)
        }

        return@withContext words.distinctBy { "${it.word}|${it.partOfSpeech}" }
    }

    private fun parseSynsetLine(line: String, pos: String): List<Word>? {
        return try {
            val parts = line.split(" ")
            if (parts.size < 6) return null

            val wordCount = parts[3].toIntOrNull() ?: return null
            val words = parts.subList(4, 4 + wordCount * 2)
                .filterIndexed { index, _ -> index % 2 == 0 }
                .map { it.replace('_', ' ') }

            val definition = line.substringAfter("|", "")
                .takeIf { it.isNotEmpty() } ?: return null

            words.map { word ->
                Word(
                    word = word.lowercase(),
                    definition = definition.trim(),
                    partOfSpeech = when (pos) {
                        "adj" -> "adjective"
                        "adv" -> "adverb"
                        else -> pos
                    }
                )
            }
        } catch (e: Exception) {
            Log.e("WordNetParser", "Error parsing line: $line", e)
            null
        }
    }

    private fun String.getFileExtension(): String = when (this) {
        "noun" -> "n"
        "verb" -> "v"
        "adj" -> "a"
        "adv" -> "r"
        else -> error("Invalid POS")
    }
}
