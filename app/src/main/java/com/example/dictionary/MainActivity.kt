// app/src/main/java/com/example/dictionary/MainActivity.kt
package com.example.dictionary

import android.os.Bundle
import android.view.KeyEvent
import android.util.Log
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
import androidx.activity.OnBackPressedCallback

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val dictionaryViewModel: DictionaryViewModel by viewModels {
        DictionaryViewModelFactory((application as DictionaryApplication).repository)
    }

    private var isSearchMode = false
    private var hasSearchResults = false

    private val backPressedCallback = object : OnBackPressedCallback(true) { // Initially enabled
        override fun handleOnBackPressed() {
            // Your existing onBackPressed logic goes here
            if (isSearchMode) {
                if (hasSearchResults || binding.etSearchInput.text.isNotEmpty()) {
                    binding.etSearchInput.text.clear() // Triggers textChangedListener
                    // The textChangedListener handles hiding results and showing welcome.
                    // If you need to explicitly disable this callback after clearing text,
                    // you can do so here: this.isEnabled = false (if appropriate)
                } else {
                    // If search input is empty and no results, then fully hide search mode
//                    hideSearchInput() // This method might also disable this callback
                    hideSearchUIAndExit()
                    // or set isSearchMode to false, which then disables the callback
                }
            } else {
                // If not in search mode, allow default back press behavior
                // To do this, you might disable this callback and then invoke onBackPressedDispatcher.onBackPressed()
                // or simply remove the callback if it's no longer needed.
                // For simple cases, if this callback shouldn't handle it,
                // you can make it disabled and the system will try other callbacks or default behavior.
                isEnabled = false // Disable this callback
                onBackPressedDispatcher.onBackPressed() // Perform default back press
                // Re-enable if needed for next time (e.g., when search mode is entered again)
                // isEnabled = true; // Or manage enablement based on isSearchMode
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
        setupSearchInput()
        onBackPressedDispatcher.addCallback(this, backPressedCallback)

        // Check database initialization status
        lifecycleScope.launch {
//            val wordCount = dictionaryViewModel.getWordCount()
//            Toast.makeText(this@MainActivity, "Dictionary loaded with $wordCount words", Toast.LENGTH_SHORT).show()
            showKeyboardForSearchInput()
        }
    }

    private fun setupUI() {
        binding.tvDictionaryLabel.text = getString(R.string.app_name)
    }

    private fun setupSearchInput() {
        binding.etSearchInput.setOnEditorActionListener { _, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH ||
                (event != null && event.keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_DOWN)) {
                performSearch()
                isSearchMode = true // Assuming performSearch enters search mode
//                backPressedCallback.isEnabled = true // Enable callback when search is active
                true
            } else {
                false
            }
        }

        // Optional: Auto-search as user types
        binding.etSearchInput.addTextChangedListener { text ->
            val searchText = text?.toString()?.trim()

            if (searchText.isNullOrEmpty()) {
                // If search text is empty, hide the results and show welcome/default state
                if (hasSearchResults) { // Only act if results were previously shown
                    hideAllResultViews() // Hide word, definition, no_results_text
                    binding.searchResultsSection.visibility = android.view.View.GONE // Hide the entire results section
                    binding.welcomeContainer.visibility = android.view.View.VISIBLE // Show welcome message
                    hasSearchResults = false // Reset the flag
                }
            } else {
                // Optional: Auto-search as user types (your existing logic)
                activateSearchMode()
                if (searchText.length >= 3) { // Or your preferred auto-search trigger length
                    performAutoSearch(searchText)
                }
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
                Log.e("MainActivity", "Auto-search error: ${e.message}")
            }
        }
    }

    private fun hideSearchUIAndExit() {
        // This is the action for "exiting search mode"
        isSearchMode = false
        hasSearchResults = false
        backPressedCallback.isEnabled = false // Disable custom callback

        // Hide keyboard
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.etSearchInput.windowToken, 0)

        // Option 1: Hide search input and show welcome (if you have other UI to go back to)
        // binding.searchSection.visibility = android.view.View.GONE
        // binding.searchResultsSection.visibility = android.view.View.GONE
        // binding.welcomeContainer.visibility = android.view.View.VISIBLE
        // binding.etSearchInput.text.clear() // Clear text

        // Option 2: Since this is likely the main activity and primary function,
        // exiting search mode might mean finishing the activity.
        // Trigger the default back press which should now finish the activity
        // because our callback is disabled.
        onBackPressedDispatcher.onBackPressed()
    }

    private fun performSearch() {
        val searchWord = binding.etSearchInput.text.toString().trim()
        if (searchWord.isNotEmpty()) {
            val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(binding.etSearchInput.windowToken, 0)
            activateSearchMode()
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
        activateSearchMode()

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
        activateSearchMode()

        binding.welcomeContainer.visibility = android.view.View.GONE
        binding.searchResultsSection.visibility = android.view.View.VISIBLE

        binding.apply {
            tvSearchedWord.visibility = android.view.View.GONE
            tvDefinition.visibility = android.view.View.GONE
            tvNoResults.visibility = android.view.View.VISIBLE
            tvNoResults.setText(R.string.word_not_found) // Or your string resource = "Word not found in dictionary"
        }
    }

    private fun hideAllResultViews() {
        binding.apply {
            tvSearchedWord.visibility = android.view.View.GONE
            tvDefinition.visibility = android.view.View.GONE
            tvNoResults.visibility = android.view.View.GONE
        }
    }

    private fun showKeyboardForSearchInput() {
        isSearchMode = true // Entering search mode
        backPressedCallback.isEnabled = true // Enable custom back handling

        binding.searchSection.visibility = android.view.View.VISIBLE // Ensure search section is visible
        binding.welcomeContainer.visibility = android.view.View.GONE

        binding.etSearchInput.requestFocus()
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        binding.etSearchInput.post {
            imm.showSoftInput(binding.etSearchInput, InputMethodManager.SHOW_IMPLICIT)
        }
    }

    private fun activateSearchMode() {
        if (!isSearchMode) { // Only change state if not already active
            isSearchMode = true
            backPressedCallback.isEnabled = true // Enable custom back handling

            binding.searchSection.visibility = android.view.View.VISIBLE
            binding.welcomeContainer.visibility = android.view.View.GONE // Hide welcome when search is active

            binding.etSearchInput.requestFocus()
            val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            binding.etSearchInput.post { // Ensures the view is ready
                imm.showSoftInput(binding.etSearchInput, InputMethodManager.SHOW_IMPLICIT)
            }
        } else {
            // If already in search mode, ensure focus and keyboard if they were lost
            if (!binding.etSearchInput.isFocused) {
                binding.etSearchInput.requestFocus()
            }
            val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            if (!imm.isAcceptingText) { // A simplistic check if keyboard is up
                binding.etSearchInput.post {
                    imm.showSoftInput(binding.etSearchInput, InputMethodManager.SHOW_IMPLICIT)
                }
            }
        }
        // Ensure welcome is hidden if search is active
        binding.welcomeContainer.visibility = android.view.View.GONE
        binding.searchSection.visibility = android.view.View.VISIBLE
    }
}
