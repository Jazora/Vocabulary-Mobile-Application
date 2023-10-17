package uk.ac.aber.dcs.cs31620.vocabulary.ui.crossword

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import uk.ac.aber.dcs.cs31620.vocabulary.R
import uk.ac.aber.dcs.cs31620.vocabulary.model.Word
import uk.ac.aber.dcs.cs31620.vocabulary.model.WordViewModel
import uk.ac.aber.dcs.cs31620.vocabulary.ui.components.ConfirmationDialogComponent
import uk.ac.aber.dcs.cs31620.vocabulary.ui.components.ScaffoldComponent
import uk.ac.aber.dcs.cs31620.vocabulary.ui.navigation.ScreenPath
import uk.ac.aber.dcs.cs31620.vocabulary.ui.navigation.navigateToScreen
import uk.ac.aber.dcs.cs31620.vocabulary.ui.theme.VocabularyTheme

/**
 * Method used for determining if the matching word is valid with the main word
 * @param lastMainCharIndex The last matching word connection point
 * @param mainCharIndex The current main char index
 * @param text The translation being compared
 * @return if the word is valid
 */
private fun wordIsValid(
    lastMainCharIndex: Int,
    mainCharIndex: Int,
    text: String
) : Boolean {
    // ensure the translation has the correct length
    if (text.length in 3..6) {
        // ensure the current main char index hasnt been matched already
        if (lastMainCharIndex != mainCharIndex) {
            //ensure the match index isnt close to the other match index
            if ((mainCharIndex - 1) != lastMainCharIndex && (mainCharIndex + 1) != lastMainCharIndex) {
                return true
            }
        }
    }

    return false
}

/**
 * This method determines if a crossword is available with the current word list
 * @param wordList The current word list
 * @param crosswordAvailable Returning value if the crossword is available
 * @param cWord1 The returning main word found
 * @param cWord2 The first matching word with the main word
 * @param cWord3 The second matching word with the main word
 */
fun canCrossword(
    wordList: List<Word>,
    crosswordAvailable: MutableState<Boolean>,
    cWord1: MutableState<Word>,
    cWord2: MutableState<MatchingWord>,
    cWord3: MutableState<MatchingWord>,
){
    /**
     * Used to find a matching word with the current main word
     * @param wordList The current word list
     * @param mainWord The main word being compared
     * @param alreadyFound An already found found word from before
     * @return The found matching word
     */
    fun findMatchingCrosswordWord(
        wordList: List<Word>,
        mainWord : Word,
        alreadyFound : MatchingWord
    ) : MatchingWord {

        //Go through the word list
        wordList.forEach { currentComparedWord ->
            if (currentComparedWord != mainWord && alreadyFound.word != currentComparedWord) {

                // iterate through each of the main word characters
                var i = 0
                mainWord.translation.forEach { c1 ->

                    //Check with the current word in the word list and check if the word is valid
                    if (wordIsValid(alreadyFound.mainCharIndex, i, currentComparedWord.translation)) {

                        //Check each character of the current word and see if it matches with the mainWord
                        var j = 0
                        currentComparedWord.translation.forEach { c2 ->

                            if (c1.lowercaseChar() == c2.lowercaseChar()) {

                                return MatchingWord(i, currentComparedWord, j) // Found match
                            }
                            j++
                        }
                    }
                    i++
                }
            }
        }

        return MatchingWord() // Found no word
    }


    //Check we have enough words available
    if (wordList.size >= 3) {
        //Shuffle the word list to get random results
        val wordListShuffled = wordList.shuffled()
        //Used to stop checking for new words
        var found = false

        //Go through each shuffled word
        wordListShuffled.forEach { mainWord ->
            //If we havent found 3 matching words continue searching
            if (!found) {
                //Find a main word
                val foundWord = findMatchingCrosswordWord(wordListShuffled, mainWord, MatchingWord())

                // If we found a word, find a first word to match
                if (foundWord.mainCharIndex != -1) { // found a word
                    val secondFoundWord = findMatchingCrosswordWord(wordListShuffled, mainWord, foundWord)

                    //if we found a first word then search for a second word
                    if (secondFoundWord.mainCharIndex != -1) {
                        //If we found all 3 words crossword is available
                        crosswordAvailable.value = true

                        cWord1.value = mainWord
                        cWord2.value = foundWord
                        cWord3.value = secondFoundWord
                        //Stop search
                        found = true
                    }
                }
            }
        }
    }
}

/**
 * Main navigation entry for the crossword screen, Handles setting up the content screen
 * @param navController The NavHostController
 * @param wordViewModel The current WordViewModel
 */
@Composable
fun CrosswordScreen(
    navController: NavHostController,
    wordViewModel: WordViewModel = viewModel()
) {
    //Get the current word list
    val wordList by wordViewModel.wordList.observeAsState(listOf())

    //Setup if crossword is available and store found words
    val crosswordAvailable = remember { mutableStateOf(false) }
    val cWord1 = remember { mutableStateOf(Word()) }
    val cWord2 = remember { mutableStateOf(MatchingWord()) }
    val cWord3 = remember { mutableStateOf(MatchingWord()) }

    //Start coroutine to check if crossword is available
    val coroutineScope = rememberCoroutineScope()
    LaunchedEffect(Unit) {
        coroutineScope.launch(Dispatchers.Main) {
            canCrossword(wordList, crosswordAvailable, cWord1, cWord2, cWord3)
        }
    }

    //The resulting words the user inputs
    val resultWord1 = rememberSaveable { ( mutableStateOf("") ) }
    val resultWord2 = rememberSaveable { ( mutableStateOf("" ) ) }
    val resultWord3 = rememberSaveable { ( mutableStateOf("") ) }

    //If to update the state
    val update = rememberSaveable { ( mutableStateOf(false)) }

    //The current first and second words to be placed on the gridmap
    val firstWord = remember { ( mutableStateOf(MatchingWord())) }
    val secondWord = remember { ( mutableStateOf(MatchingWord())) }

    //The amount of correct answers
    val correctAnswers = rememberSaveable { ( mutableStateOf(0)) }
    //Score popup
    val answerPopUp = rememberSaveable { ( mutableStateOf(false)) }

    ScaffoldComponent(
        navController = navController,
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    //Check all inputted characters to ensure they match with the main word
                    resultWord1.value.lowercase().forEachIndexed { i, char ->
                        if (char == cWord1.value.translation[i].lowercaseChar()) {
                            //Add to total score
                            correctAnswers.value++
                        }
                    }

                    //Determine the first word
                    val compareFirstWord = (if (firstWord.value.word.id == cWord2.value.word.id) cWord2
                                            else cWord3)
                    //Check all inputted characters to ensure they match with the first word
                    resultWord2.value.lowercase().forEachIndexed { i, char ->
                        if (char == compareFirstWord.value.word.translation[i].lowercaseChar()) {
                            //Add to total score
                            correctAnswers.value++
                        }
                    }

                    //Determine the second word
                    val compareSecondWord = (if (firstWord.value.word.id == cWord2.value.word.id) cWord3
                                            else cWord2)
                    //Check all inputted characters to ensure they match with the second word
                    resultWord3.value.lowercase().forEachIndexed { i, char ->
                        if (char == compareSecondWord.value.word.translation[i].lowercaseChar()) {
                            //Add to total score
                            correctAnswers.value++
                        }
                    }

                    //Show score
                    answerPopUp.value = true
                },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = stringResource(R.string.language_select_continue),
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    ) {
        //After the coroutine has determined if crossword is available start the screen
        if (crosswordAvailable.value) {

            //find the first word to be displayed onto the grid
            if (cWord2.value.wordCharIndex > cWord3.value.wordCharIndex) {
                firstWord.value = cWord2.value
                secondWord.value = cWord3.value
            } else {
                firstWord.value = cWord3.value
                secondWord.value = cWord2.value
            }

            val setup = rememberSaveable { ( mutableStateOf(true)) }

            //first time setup
            if (setup.value) {
                // blank all the values for each text input
                resultWord1.value = cWord1.value.translation
                val resultWord1CharArray = resultWord1.value.toCharArray()
                resultWord1CharArray.forEachIndexed { index, _ ->
                    resultWord1CharArray[index] = ' '
                }
                resultWord1.value = String(resultWord1CharArray)

                resultWord2.value = firstWord.value.word.translation
                val resultWord2CharArray = resultWord2.value.toCharArray()
                resultWord2CharArray.forEachIndexed { index, _ ->
                    resultWord2CharArray[index] = ' '
                }
                resultWord2.value = String(resultWord2CharArray)

                resultWord3.value = secondWord.value.word.translation
                val resultWord3CharArray = resultWord3.value.toCharArray()
                resultWord3CharArray.forEachIndexed { index, _ ->
                    resultWord3CharArray[index] = ' '
                }
                resultWord3.value = String(resultWord3CharArray)

                setup.value = false
            }

            //The total answer count
            val totalAnswers = cWord1.value.translation.length + cWord2.value.word.translation.length + cWord3.value.word.translation.length

            //Start the crossword content
            CrosswordContent(it, cWord1.value.translation, firstWord.value, secondWord.value, resultWord1, resultWord2, resultWord3, update,
                answerPopUp, correctAnswers.value, totalAnswers, onClickConfirmPopUp = {
                    answerPopUp.value = false
                    //Navigate to the home screen on completion
                    navigateToScreen(navController, ScreenPath.Home.path, false)
                }
            )
        }
    }
}

/**
 * Method used to update a result input and insert a new character
 * @param charIndex the char index to replace
 * @param currentResult the current result to be updated
 * @param char the char to insert
 */
private fun updateWord(
    charIndex: Int,
    currentResult: MutableState<String>,
    char: Char
) {
    val wordCharList = currentResult.value.toCharArray()
    wordCharList[charIndex] = char
    currentResult.value = String(wordCharList)
}

/**
 * The text field for each char element, Used to update the appropriate result values
 * @param text Current text display in the text box
 * @param index Current index within the grid index array
 * @param indexList1 MainWord grid index list
 * @param indexList2 FirstWord grid index list
 * @param indexList3 SecondWord grid index list
 * @param result1 MainWord result input
 * @param result2 FirstWord result input
 * @param result3 SecondWord result input
 * @param update Update the state
 */
@Composable
private fun CrosswordTextField(
    text: String,
    index: Int,
    indexList1: IntArray,
    indexList2: IntArray,
    indexList3: IntArray,
    result1: MutableState<String>,
    result2: MutableState<String>,
    result3: MutableState<String>,
    update: MutableState<Boolean>
) {
    OutlinedTextField(
        modifier = Modifier,
        value = text.trim(),
        label = {},
        singleLine = true,
        textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center),
        onValueChange = {
            //Ensure the input is not empty
            if (it.isNotEmpty()) {
                if (indexList1.contains(index)) { // update main word if exists in its grid indexes
                    updateWord(indexList1.indexOf(index), result1, it.last())
                }

                if (indexList2.contains(index)) { // update first word if exists in its grid indexes
                    updateWord(indexList2.indexOf(index), result2, it.last())
                }

                if (indexList3.contains(index)) { // update second word if exists in its grid indexes
                    updateWord(indexList3.indexOf(index), result3, it.last())
                }

                //update the value
                update.value = true
            }
        }
    )
}

/**
 * Used to display the Crossword UI elements
 * @param paddingValues The padding values of the ScaffoldComponent
 * @param mainWord The current main word
 * @param firstWord The first word attached to main word
 * @param secondWord The second word attached to main word
 * @param resultWord1 The main word result from user input
 * @param resultWord2 The first word result from user input
 * @param resultWord3 The second word result from user input
 * @param update Update the state
 * @param answerPopUp Show score
 * @param correctAnswers The amount of correct answers
 * @param totalAnswers The total count of characters
 * @param onClickConfirmPopUp Onclick of the score popup
 */
@Composable
private fun CrosswordContent(
    paddingValues: PaddingValues,
    mainWord: String,
    firstWord: MatchingWord,
    secondWord: MatchingWord,
    resultWord1: MutableState<String>,
    resultWord2: MutableState<String>,
    resultWord3: MutableState<String>,
    update: MutableState<Boolean>,
    answerPopUp: MutableState<Boolean>,
    correctAnswers: Int,
    totalAnswers: Int,
    onClickConfirmPopUp: () -> Unit
) {
    //Find the grid height
    val gridHeight = mainWord.length
    //Find the grid width
    val gridWidth = firstWord.wordCharIndex + (secondWord.word.translation.length - secondWord.wordCharIndex)
    //Get the total amount of cells in the grid
    val gridCells = gridHeight * gridWidth

    //Ensure the main word has some empty characters
    if (resultWord1.value.isNotEmpty()){

        Column(
            modifier = Modifier.padding(paddingValues)

        ) {
            //Title the screen
            Text(text = stringResource(R.string.crossword_title), modifier = Modifier.align(Alignment.CenterHorizontally),
                fontWeight = FontWeight.Bold, fontSize = 35.sp)

            //Create the grid
            LazyVerticalGrid(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(vertical = 10.dp),
                columns = GridCells.Fixed(gridWidth),
                contentPadding = PaddingValues(10.dp),
                verticalArrangement = Arrangement.spacedBy(5.dp),
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ){
                //Get the biggest match index for starting point
                val mainWordStartX = firstWord.wordCharIndex

                //add all grid indexes for the main word
                val mainWordIndexes = IntArray(gridHeight)
                mainWordIndexes.forEachIndexed { i, _ ->
                    mainWordIndexes[i] = mainWordStartX + (i * gridWidth)
                }

                //Find the starting index for the first word
                val firstWordStartY = mainWordStartX + (firstWord.mainCharIndex * gridWidth)
                //add all grid indexes for the first word
                val firstWordIndexes = IntArray(firstWord.word.translation.length)
                firstWordIndexes.forEachIndexed { i, _ ->
                    firstWordIndexes[i] = (firstWordStartY - firstWord.wordCharIndex) + i
                }

                //Find the starting index for the second word
                val secondWordStartY = mainWordStartX + (secondWord.mainCharIndex * gridWidth)
                //add all grid indexes for the second word
                val secondWordIndexes = IntArray(secondWord.word.translation.length)
                secondWordIndexes.forEachIndexed { i, _ ->
                    secondWordIndexes[i] = (secondWordStartY - secondWord.wordCharIndex) + i
                }

                //iterate through each grid index of the grid
                itemsIndexed(IntArray(gridCells).toList()) { i, _ ->
                    //If any of the grid indexes matches with any of the words indexes create a text box
                    if (mainWordIndexes.contains(i)) {
                        CrosswordTextField(resultWord1.value.toCharArray()[mainWordIndexes.indexOf(i)].toString(),  i,
                            mainWordIndexes, firstWordIndexes, secondWordIndexes, resultWord1, resultWord2, resultWord3, update)
                    } else if (firstWordIndexes.contains(i)) {
                        CrosswordTextField(resultWord2.value.toCharArray()[firstWordIndexes.indexOf(i)].toString(),  i,
                            mainWordIndexes, firstWordIndexes, secondWordIndexes, resultWord1, resultWord2, resultWord3, update)
                    } else if (secondWordIndexes.contains(i)) {
                        CrosswordTextField(resultWord3.value.toCharArray()[secondWordIndexes.indexOf(i)].toString(),  i,
                            mainWordIndexes, firstWordIndexes, secondWordIndexes, resultWord1, resultWord2, resultWord3, update)
                    } else {
                        //if not place an empty text
                        Text(text = "")
                    }
                }
            }
        }

        //If the score popup is being shown
        if (answerPopUp.value) {
            //Do confirm pop up with score
            ConfirmationDialogComponent(
                stringResource(R.string.dialog_your_score),
                { Text(text = "$correctAnswers/$totalAnswers", fontSize = 30.sp, fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth()) },
                stringResource(R.string.popup_confirm),
                "",
                onDismissRequest = onClickConfirmPopUp,
                dismissButton = onClickConfirmPopUp,
                confirmButton = onClickConfirmPopUp
            )
        }
    }
}

/**
 * The crossword screen preview
 */
@Preview
@Composable
fun CrosswordScreenPreview() {
    val navController = rememberNavController()

    VocabularyTheme {
        CrosswordScreen(navController)
    }
}