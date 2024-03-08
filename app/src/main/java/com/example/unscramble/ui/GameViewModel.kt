package com.example.unscramble.ui


import android.util.Log
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.unscramble.data.MAX_NO_OF_WORDS
import com.example.unscramble.data.SCORE_INCREASE
import com.example.unscramble.data.WordsData
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.util.Locale

class GameViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()
    private lateinit var currentWord: String
    private var count = 1
    private var timeLeft by mutableIntStateOf(180)
    private val wordList: MutableList<WordsData> = mutableListOf(
        WordsData("animal", false),
        WordsData("apple", false),
        WordsData("basket", false),
        WordsData("mother", false),
        WordsData("dog", false),
        WordsData("all", false),
        WordsData("balloon", false),
        WordsData("awesome", false),
        WordsData("arise", false),
        WordsData("anecdote", false),
        WordsData("zone", false)

    )
    private val wordStrings: List<String> = wordList.map { it.word }


    var userGuess by mutableStateOf("")
    private var usedWords: MutableSet<String> = mutableSetOf()

    init {
        resetGame()


    }

    fun updateUserGuess(guessedWord: String) {
        userGuess = guessedWord
    }

    private fun pickRandomWordAndShuffle(): String {
        do {
            currentWord = wordStrings.random()
        } while (usedWords.contains(currentWord))

        usedWords.add(currentWord)
        return shuffleCurrentWord(currentWord)
    }

    fun checkUserGuess() {
        if (userGuess.trim().lowercase(Locale.getDefault()) in wordStrings) {
            val updatedScore = _uiState.value.score.plus(SCORE_INCREASE)

            val correctWordData =
                wordList.find { it.word == userGuess.trim().lowercase(Locale.getDefault()) }

            // Update the boolean value to true if the word is found
            correctWordData?.let {
                it.isUserGuessed = true
            }


            updateGameState(updatedScore)
        } else {
            _uiState.update { currentState ->
                currentState.copy(isGuessedWordWrong = true)
            }
        }

        updateUserGuess("")
    }

    private fun updateGameState(updatedScore: Int) {
        val currentWordCount = _uiState.value.currentWordCount
        if (currentWordCount == MAX_NO_OF_WORDS) {
            _uiState.update { currentState ->
                currentState.copy(
                    isGuessedWordWrong = false,
                    score = updatedScore,
//                    isGameOver = true
                )
            }
        } else {
            _uiState.update { currentState ->
                currentState.copy(
                    isGuessedWordWrong = false,
                    currentScrambledWord = pickRandomWordAndShuffle(),
                    score = updatedScore,
                    currentWordCount = currentState.currentWordCount.inc(),
                )
            }
        }
    }



    private fun shuffleCurrentWord(word: String): String {
        val tempWord = word.toCharArray()
        // Scramble the word
        tempWord.shuffle()
        while (String(tempWord) == word) {
            tempWord.shuffle()
        }
        return String(tempWord)
    }

    fun resetGame() {
        
        timeLeft = 180
        startCountdown()

        usedWords.clear()
        _uiState.value = GameUiState(currentScrambledWord = pickRandomWordAndShuffle())
    }

    fun doneGame() {
        _uiState.update { currentState ->
            currentState.copy(
                isGuessedWordWrong = false,
                isGameOver = true
            )
        }
    }


    fun next() {
        saveNextWordCount(_uiState.value.score)
        updateUserGuess("")

    }
    fun checkUserGuessed(): Boolean {
        val correctWordData =
            wordList.find { it.word == userGuess.trim().lowercase(Locale.getDefault()) }

        return correctWordData!!.isUserGuessed
    }

    private fun saveNextWordCount(updatedScore: Int) {
        count = (count + 1) % wordStrings.size
        _uiState.update { currentState ->
            currentState.copy(
                currentScrambledWord = wordStrings[count],
                isGuessedWordWrong = false,
                score = updatedScore,
                currentWordCount = currentState.currentWordCount.inc(),
            )
        }
    }


    fun previous() {
        savePreviousWordCount(_uiState.value.score)
        updateUserGuess("")

    }

    private fun savePreviousWordCount(updatedScore: Int) {
        count = (count - 1 + wordStrings.size) % wordStrings.size
        _uiState.update { currentState ->
            currentState.copy(
                currentScrambledWord = wordStrings[count],
                isGuessedWordWrong = false,
                score = updatedScore,
                currentWordCount = currentState.currentWordCount.dec(),
            )
        }
    }

    fun checkPreviousCurrentWordCount(): Boolean {
        val currentWordCount = _uiState.value.currentWordCount
        return currentWordCount != 1
    }

    fun checkNextCurrentWordCount(): Boolean {
        val currentWordCount = _uiState.value.currentWordCount
        return currentWordCount != 10
    }
    private fun startCountdown() {
        viewModelScope.launch {
            while (timeLeft > 0) {
                delay(1000L)
                timeLeft--
            }
        }
    }
    fun getTime(): String {
        val minutes = timeLeft / 60
        val seconds = timeLeft % 60
        if(minutes==0 && seconds==0){
            doneGame()
        }
        return "%02d:%02d".format(minutes, seconds)
    }
}