package com.example.unscramble.ui


import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
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
import java.util.Locale

class GameViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    private var count by mutableIntStateOf(0)
    private var timeLeft by mutableIntStateOf(180)
    private var isWordGuessed by mutableStateOf(false)
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


    fun checkUserGuess() {
        if (userGuess.trim().lowercase(Locale.getDefault()) in wordStrings) {
            val updatedScore = _uiState.value.score.plus(SCORE_INCREASE)

            val correctWordData =
                wordList.find { it.word == userGuess.trim().lowercase(Locale.getDefault()) }
            correctWordData?.let {
                it.isUserGuessed = true
                isWordGuessed = true
            }

            updateGameState(updatedScore)
        } else {
            _uiState.update { currentState ->
                currentState.copy(isGuessedWordWrong = true)
            }
        }

        updateUserGuess("")
    }

    fun canSubmit(): Boolean {
        val currentWordData = wordList.getOrNull(count - 1)
        return currentWordData?.isUserGuessed == false
    }


    private fun updateGameState(updatedScore: Int) {
        val currentWordCount = _uiState.value.currentWordCount
        if (currentWordCount == MAX_NO_OF_WORDS) {
            _uiState.update { currentState ->
                currentState.copy(
                    isGuessedWordWrong = false,
                    score = updatedScore,
                )
            }
        } else {
            _uiState.update { currentState ->
                currentState.copy(
                    isGuessedWordWrong = false,
                    currentScrambledWord = nextWord(),
                    score = updatedScore,
                    currentWordCount = count,
                )
            }
        }
        if (checkAllWordsGuessed()) {
            doneGame()
        }
    }

    private fun shuffleCurrentWord(word: String): String {
        val tempWord = word.toCharArray()
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
        updateUserGuess("")
        count = 0
        wordList.forEach { it.isUserGuessed = false }
        _uiState.value = GameUiState(currentScrambledWord = nextWord(), currentWordCount = 1)
    }

    private fun nextWord(): String {
        val word = wordStrings[count]
        count = (count + 1) % wordStrings.size
        return shuffleCurrentWord(word)
    }

    fun doneGame() {
        _uiState.update { currentState ->
            currentState.copy(
                isGuessedWordWrong = false,
                isGameOver = true
            )
        }
    }

    fun checkAllWordsGuessed(): Boolean {
        return wordList.count { it.isUserGuessed } == MAX_NO_OF_WORDS
    }


    fun next() {
        if (count < wordStrings.size - 1) {
            count++
            updateGameStateWithCount(_uiState.value.score)
            updateUserGuess("")
        }
    }


    fun previous() {
        if (count > 0) {
            count--
            updateGameStateWithCount(_uiState.value.score)
            updateUserGuess("")
        }
    }

    private fun updateGameStateWithCount(updatedScore: Int) {
        _uiState.update { currentState ->
            currentState.copy(
                currentScrambledWord = shuffleCurrentWord(wordStrings[count - 1]),
                isGuessedWordWrong = false,
                score = updatedScore,
                currentWordCount = count,
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
        if (minutes == 0 && seconds == 0) {
            doneGame()
        }
        return "%02d:%02d".format(minutes, seconds)
    }
}