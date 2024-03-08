package com.example.unscramble.data

const val MAX_NO_OF_WORDS = 10
const val SCORE_INCREASE = 20

// Set with all the words for the Game
val allWords: MutableList<String> =
    mutableListOf(
        "animal",
        "auto",
        "anecdote",
        "alphabet",
        "all",
        "awesome",
        "arise",
        "balloon",
        "basket",
        "bench",
        "zoology",
        "zone",
        "zeal"
    )

class WordsData(
    val word:String,
    var isUserGuessed:Boolean = false
) {

}