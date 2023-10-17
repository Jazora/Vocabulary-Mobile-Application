package uk.ac.aber.dcs.cs31620.vocabulary.ui.matchword

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CutCornerShape
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
import uk.ac.aber.dcs.cs31620.vocabulary.R
import uk.ac.aber.dcs.cs31620.vocabulary.model.Word
import uk.ac.aber.dcs.cs31620.vocabulary.model.WordViewModel
import uk.ac.aber.dcs.cs31620.vocabulary.ui.components.ConfirmationDialogComponent
import uk.ac.aber.dcs.cs31620.vocabulary.ui.components.ScaffoldComponent
import uk.ac.aber.dcs.cs31620.vocabulary.ui.navigation.ScreenPath
import uk.ac.aber.dcs.cs31620.vocabulary.ui.navigation.navigateToScreen
import uk.ac.aber.dcs.cs31620.vocabulary.ui.theme.VocabularyTheme
import kotlin.math.roundToInt

/**
 * Used for determining if the match word quiz is available
 * @param wordList current word list
 * @return If match word quiz is available
 */
fun canMatchWord(
    wordList: List<Word>
) : Boolean {
    if (wordList.size >= 3) { // If more than 3 words allow quiz
        return true
    }

    return false
}

/**
 * Match word screen main entry point
 * @param navController The current NavHostController
 * @param wordViewModel The current WordViewModel
 */
@Composable
fun MatchWordScreen(
    navController: NavHostController,
    wordViewModel: WordViewModel = viewModel()
) {
    ScaffoldComponent(
        navController = navController,
    ) {
        // get the word list from WordViewModel
        val wordList by wordViewModel.wordList.observeAsState(listOf())
        // The amount of questions picked by the user
        val questionsCount = rememberSaveable { ( mutableStateOf(0)) }
        //The amount of words available
        val maxQuestions = wordList.size

        //On first state
        if (questionsCount.value == 0) {
            //Start the popup for question count
            MatchWordSetup(questionsCount, maxQuestions, onCancelClick = {
                navigateToScreen(navController, ScreenPath.Home.path, false)
            })
        } else { // After questions count has been picked

            //shuffle the words
            val shuffledWords = rememberSaveable { ( mutableStateOf(wordList.shuffled())) }
            //set a current word
            val currentWord = remember { ( mutableStateOf(Word()) ) }

            // The current index that is the answer
            val answerIndex = rememberSaveable { ( mutableStateOf(0)) }
            // Current selected answer
            val selectedAnswer = rememberSaveable { ( mutableStateOf("") ) }
            val answerPopUp = remember { mutableStateOf(false) }
            // The count of correct answers
            val correctAnswers = rememberSaveable { ( mutableStateOf(0) ) }

            LaunchedEffect(Unit) {
                if (wordList.size >= questionsCount.value) {
                    currentWord.value = shuffledWords.value[answerIndex.value] // set the first current word
                }
            }

            var shuffledTranslations = listOf("")

            if (wordList.size >= 3) {
                var shuffledWords = wordList.shuffled()

                //if the first and second word are not the current first word reshuffle
                while(shuffledWords[0].id == currentWord.value.id || shuffledWords[1].id == currentWord.value.id) {
                    shuffledWords = wordList.shuffled()
                }

                // The different answers available to the user
                val translationStrings = listOf(currentWord.value.translation, shuffledWords[0].translation,
                    shuffledWords[1].translation)

                //Shuffle answers
                shuffledTranslations = translationStrings.shuffled()
            }

            // Start match word content
            MatchWordContent(it, currentWord, shuffledTranslations, selectedAnswer, answerPopUp, correctAnswers,
                questionsCount.value, onClickMatchButton = {
                    // if the current word is the selected answer add 1 to correct answers
                    if (currentWord.value.translation == selectedAnswer.value) {
                        correctAnswers.value++
                    }

                    answerIndex.value++
                    //If reached on the amount of questions, finish
                    if (answerIndex.value >= questionsCount.value) {
                        answerPopUp.value = true
                    } else {
                        // Change current word to continue to next question
                        currentWord.value = shuffledWords.value[answerIndex.value]
                    }
                },
                onClickConfirmPopUp = {
                    answerPopUp. value = false
                    //Go back to main menu
                    navigateToScreen(navController, ScreenPath.Home.path, false)
                }
            )
        }
    }
}

/**
 * This method opens a popup to ask the user how many questions they would like to do
 * @param questionsCount mutable state we wish to set
 * @param maxQuestions the max amount of questions available
 */
@Composable
private fun MatchWordSetup(
    questionsCount: MutableState<Int>,
    maxQuestions: Int,
    onCancelClick: () -> Unit
) {
    //Slider value
    val sliderCount = rememberSaveable { ( mutableStateOf(0f)) }

    //Start confirmationDialogComponent
    ConfirmationDialogComponent(
        stringResource(R.string.popup_matchword_questioncount_title),
        { Slider(value = sliderCount.value, onValueChange = {
                sliderCount.value = it
                sliderCount.value.roundToInt()
            }, modifier = Modifier, valueRange = 0f..maxQuestions.toFloat())
            Text(text = sliderCount.value.toInt().toString())
        },
        stringResource(R.string.popup_matchword_questioncount_start),
        stringResource(R.string.popup_cancel),
        onDismissRequest = onCancelClick,
        confirmButton = { questionsCount.value = sliderCount.value.toInt() },
        dismissButton = onCancelClick
    )
}

/**
 * The answer button within the Match Word quiz
 * @param modifier The current modifier
 * @param text The buttons text
 * @param selectedAnswer mutable state of the selected answer
 * @param onClick The onclick method
 */
@Composable
private fun MatchWordButton(
    modifier: Modifier = Modifier,
    text: String,
    selectedAnswer: MutableState<String>,
    onClick: () -> Unit
) {
    Button(
        modifier = modifier,
        onClick = {
            selectedAnswer.value = text
            onClick.invoke()
        },
        colors = ButtonDefaults.buttonColors(
            containerColor = (MaterialTheme.colorScheme.error)
        ),
        shape = CutCornerShape(0.dp)
    ){
        Text(text)
    }
}

/**
 * Match word content screen used for only UI elements
 * @param paddingValues Padding values of the ScaffoldComponent
 * @param currentWord The current word being tested
 * @param answerChoice Current different answer choices
 * @param selectedAnswer The current selected answer
 * @param answerPopUp Whether to popup the score
 * @param correctAnswers The current amount of correct answers
 * @param questionsCount The total questions of the quiz
 * @param onClickMatchButton Onclick for the answer buttons
 * @param onClickConfirmPopUp Onclick for score popup
 */
@Composable
private fun MatchWordContent(
    paddingValues: PaddingValues,
    currentWord: MutableState<Word>,
    answerChoice: List<String>,
    selectedAnswer: MutableState<String>,
    answerPopUp: MutableState<Boolean>,
    correctAnswers: MutableState<Int>,
    questionsCount: Int,
    onClickMatchButton: () -> Unit,
    onClickConfirmPopUp: () -> Unit
) {
    Column(
        Modifier.fillMaxSize().padding(paddingValues),
    ) {
        //Title
        Text(text = stringResource(R.string.matchword_title), modifier = Modifier.align(Alignment.CenterHorizontally),
            fontWeight = FontWeight.Bold, fontSize = 35.sp)

        //Column used for the contents of the page
        LazyColumn(
            Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            contentPadding = PaddingValues(horizontal = 10.dp)
        ){
            //Question text
            item {
                Text(currentWord.value.original, fontWeight = FontWeight.Bold, fontSize = 25.sp)
                Spacer(Modifier.height(40.dp))
            }

            //Answer buttons
            itemsIndexed(answerChoice) { i, _ ->
                MatchWordButton(
                    modifier = Modifier.fillMaxWidth(),
                    text = answerChoice[i],
                    selectedAnswer = selectedAnswer,
                    onClick = onClickMatchButton,
                )
            }
        }
    }

    //If reached the end of the quiz open the score popup
    if (answerPopUp.value) {
        val correctAnswersValue = correctAnswers.value

        //Open the score popup
        ConfirmationDialogComponent(
            stringResource(R.string.dialog_your_score),
            { Text(text = "$correctAnswersValue/$questionsCount", fontSize = 30.sp, fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth()) },
            stringResource(R.string.popup_confirm),
            "",
            onDismissRequest = onClickConfirmPopUp,
            dismissButton = onClickConfirmPopUp,
            confirmButton = onClickConfirmPopUp
        )
    }
}

/**
 * Preview of the Match Word quiz screen
 */
@Preview
@Composable
fun MatchWordScreenPreview() {
    val navController = rememberNavController()

    VocabularyTheme {
        MatchWordScreen(navController)
    }
}

