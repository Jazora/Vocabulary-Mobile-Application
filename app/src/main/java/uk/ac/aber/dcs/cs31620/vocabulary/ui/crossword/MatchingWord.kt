package uk.ac.aber.dcs.cs31620.vocabulary.ui.crossword

import uk.ac.aber.dcs.cs31620.vocabulary.model.Word

/**
 * Match word data class, used for determining matching character for the crossword
 */
data class MatchingWord(
    val mainCharIndex: Int = -1, // The main char connection point
    val word: Word = Word(), // The current word being matched
    val wordCharIndex: Int = -1 // The current word matching index
)