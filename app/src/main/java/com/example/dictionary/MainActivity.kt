// app/src/main/java/com/example/dictionary/MainActivity.kt
package com.example.dictionary

import android.content.Context
import android.os.Bundle
import android.view.KeyEvent
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.lifecycleScope
import com.example.dictionary.databinding.ActivityMainBinding
import com.example.dictionary.viewmodel.DictionaryViewModel
import com.example.dictionary.viewmodel.DictionaryViewModelFactory
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val dictionaryViewModel: DictionaryViewModel by viewModels {
        DictionaryViewModelFactory((application as DictionaryApplication).repository)
    }

    private var isSearchMode = false
    private var hasSearchResults = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
        setupSearchInput()

        // Check database initialization status
        lifecycleScope.launch {
//            val wordCount = dictionaryViewModel.getWordCount()
//            Toast.makeText(this@MainActivity, "Dictionary loaded with $wordCount words", Toast.LENGTH_SHORT).show()
            showKeyboardForSearchInput()
        }
    }

    private fun setupUI() {
        binding.tvDictionaryLabel.text = getString(R.string.app_name)

//        binding.searchSection.visibility = android.view.View.VISIBLE
//        binding.welcomeContainer.visibility = android.view.View.GONE
    }

    private fun setupSearchInput() {
        binding.etSearchInput.setOnEditorActionListener { _, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH ||
                (event != null && event.keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_DOWN)) {
                performSearch()
                true
            } else {
                false
            }
        }

        // Optional: Auto-search as user types
        binding.etSearchInput.addTextChangedListener { text ->
            val searchText = text?.toString()?.trim()
            if (!searchText.isNullOrEmpty() && searchText.length >= 3) {
                performAutoSearch(searchText)
            }
        }
    }

    private fun performAutoSearch(searchText: String) {
        lifecycleScope.launch {
            try {
                val words = dictionaryViewModel.searchWords(searchText)
                if (words.isNotEmpty()) {
                    val firstMatch = words.first()
                    displayWordResult(firstMatch.word, firstMatch.definition, firstMatch.partOfSpeech)
                }
            } catch (e: Exception) {
                // Ignore auto-search errors
            }
        }
    }

    private fun showSearchInput() {
        isSearchMode = true

        binding.searchSection.visibility = android.view.View.VISIBLE

        if (!hasSearchResults) {
            binding.welcomeContainer.visibility = android.view.View.GONE
        }

        binding.etSearchInput.requestFocus()

        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(binding.etSearchInput, InputMethodManager.SHOW_IMPLICIT)

    }

    private fun hideSearchInput() {
        isSearchMode = false
        hasSearchResults = false

        binding.searchSection.visibility = android.view.View.GONE
        binding.searchResultsSection.visibility = android.view.View.GONE
        binding.welcomeContainer.visibility = android.view.View.VISIBLE

        binding.etSearchInput.text.clear()
        hideAllResultViews()

        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.etSearchInput.windowToken, 0)

    }

    private fun performSearch() {
        val searchWord = binding.etSearchInput.text.toString().trim()
        if (searchWord.isNotEmpty()) {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(binding.etSearchInput.windowToken, 0)

            searchWordInDatabase(searchWord)
        } else {
            Toast.makeText(this, "Please enter a word to search", Toast.LENGTH_SHORT).show()
        }
    }

    private fun searchWordInDatabase(word: String) {
        lifecycleScope.launch {
            try {
                val foundWord = dictionaryViewModel.getWordExact(word.lowercase())
                if (foundWord != null) {
                    displayWordResult(foundWord.word, foundWord.definition, foundWord.partOfSpeech)
                } else {
                    val similarWords = dictionaryViewModel.searchWords(word.lowercase())
                    if (similarWords.isNotEmpty()) {
                        val bestMatch = similarWords.first()
                        displayWordResult(bestMatch.word, bestMatch.definition, bestMatch.partOfSpeech)
                    } else {
                        showNoResultsFound()
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(this@MainActivity, "Error searching: ${e.message}", Toast.LENGTH_SHORT).show()
                showNoResultsFound()
            }
        }
    }

    private fun displayWordResult(word: String, definition: String, partOfSpeech: String?) {
        hasSearchResults = true

        binding.welcomeContainer.visibility = android.view.View.GONE
        binding.searchResultsSection.visibility = android.view.View.VISIBLE

        binding.apply {
            val displayWord = word.replaceFirstChar { it.uppercase() }
            val fullText = if (partOfSpeech != null) {
                "$displayWord ($partOfSpeech)"
            } else {
                displayWord
            }

            tvSearchedWord.text = fullText
            tvDefinition.text = definition

            tvSearchedWord.visibility = android.view.View.VISIBLE
            tvDefinition.visibility = android.view.View.VISIBLE
            tvNoResults.visibility = android.view.View.GONE
        }
    }

    private fun showNoResultsFound() {
        hasSearchResults = true

        binding.welcomeContainer.visibility = android.view.View.GONE
        binding.searchResultsSection.visibility = android.view.View.VISIBLE

        binding.apply {
            tvSearchedWord.visibility = android.view.View.GONE
            tvDefinition.visibility = android.view.View.GONE
            tvNoResults.visibility = android.view.View.VISIBLE
            tvNoResults.text = "Word not found in dictionary"
        }
    }

    private fun hideAllResultViews() {
        binding.apply {
            tvSearchedWord.visibility = android.view.View.GONE
            tvDefinition.visibility = android.view.View.GONE
            tvNoResults.visibility = android.view.View.GONE
        }
    }

    override fun onBackPressed() {
        if (isSearchMode) {
            hideSearchInput()
        } else {
            super.onBackPressed()
        }
    }

    private fun showKeyboardForSearchInput() {
        binding.etSearchInput.requestFocus()
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        // Consider a small delay if the window isn't ready immediately, though often not needed
        binding.etSearchInput.post { // Ensures the view is ready
            imm.showSoftInput(binding.etSearchInput, InputMethodManager.SHOW_IMPLICIT)
        }
    }
}
