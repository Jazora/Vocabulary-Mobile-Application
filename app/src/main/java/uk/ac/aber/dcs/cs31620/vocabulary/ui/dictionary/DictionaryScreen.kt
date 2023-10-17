package uk.ac.aber.dcs.cs31620.vocabulary.ui.dictionary

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import uk.ac.aber.dcs.cs31620.vocabulary.R
import uk.ac.aber.dcs.cs31620.vocabulary.datasource.VocabRepository
import uk.ac.aber.dcs.cs31620.vocabulary.model.Word
import uk.ac.aber.dcs.cs31620.vocabulary.model.WordViewModel
import uk.ac.aber.dcs.cs31620.vocabulary.ui.components.ConfirmationDialogComponent
import uk.ac.aber.dcs.cs31620.vocabulary.ui.components.ScaffoldComponent
import uk.ac.aber.dcs.cs31620.vocabulary.ui.theme.VocabularyTheme

/**
 * The get word method used to get a word from the word list
 * @param wordList The current word list
 * @param wordID The word ID to match
 * @return The word returned
 */
private fun getWord(
    wordList: List<Word>,
    wordID: Int
) : Word {
    wordList.forEach{
        if (it.id == wordID) {
            return it
        }
    }

    return Word()
}

/**
 * The entry point for navigating to the Dictionary Screen, Sets up variables for Dictionary Content
 * @param navController The current NavHostController
 * @param settings The configuration file
 * @param wordViewModel The current WordViewModel
 */
@Composable
fun DictionaryScreen(
    navController: NavHostController,
    settings: SharedPreferences,
    wordViewModel: WordViewModel = viewModel()
) {
    //Get the coroutine scope
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current.applicationContext
    val repository = VocabRepository(context as Application)

    //Get the list of words from the database
    val wordList by wordViewModel.wordList.observeAsState(listOf())

    //Setup the add word popup
    val addWordPopUp = remember { mutableStateOf(false) }
    //Setup the edit word popup
    val editWordPopUp = remember { mutableStateOf(false) }
    val wordID = rememberSaveable { ( mutableStateOf(-1)) }
    val wordInput = rememberSaveable { ( mutableStateOf("")) }
    val wordInput2 = rememberSaveable { ( mutableStateOf("")) }

    //Setup the snackbar hoststate
    val snackbarHostState = remember { SnackbarHostState() }

    ScaffoldComponent(
        navController = navController,
        //Setup the add word floating action button
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    addWordPopUp.value = true
                },
                containerColor = MaterialTheme.colorScheme.secondary
            ) {
                Icon(
                    imageVector = (if (addWordPopUp.value) Icons.Filled.Add else Icons.Outlined.Add),
                    contentDescription = stringResource(R.string.dictionary_addword_floatingbutton),
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        },
        snackbarHostState = snackbarHostState,
        //Setup the snackbar
        snackbarContent = { Snackbar( snackbarData = it ) }
    ) {
        val firstLanguage = stringResource(R.string.firstLanguage)
        val secondLanguage = stringResource(R.string.secondLanguage)
        val emptyString = stringResource(R.string.emptyString)

        //Get the language settings for the content titles
        val original by remember {
            mutableStateOf(settings.getString(firstLanguage, emptyString))
        }

        val translation by remember {
            mutableStateOf(settings.getString(secondLanguage, emptyString))
        }

        //method for clearing input
        fun clearInput() {
            wordInput.value = ""
            wordInput2.value = ""
        }

        val dismissText = stringResource(R.string.snackbar_dismiss)

        //Start Dictionary Content
        DictionaryContent(it, original.toString(), translation.toString(),
            wordList, addWordPopUp, editWordPopUp, wordID, wordInput, wordInput2,
            addWordConfirmButton = { // Add word confirmation button
                val wordInputTrimmed = wordInput.value.trim()
                val wordInput2Trimmed = wordInput2.value.trim()

                //Ensure the inputs are not empty
                if (wordInputTrimmed.isNotEmpty() && wordInput2Trimmed.isNotEmpty()) {
                    addWordPopUp. value = false

                    //launch coroutine to add word to the database and start a snackbar
                    coroutineScope.launch(Dispatchers.IO) {
                        //Insert word
                        repository.insertWord(Word(original = wordInputTrimmed, translation = wordInput2Trimmed))
                        clearInput()

                        snackbarHostState.showSnackbar(
                            message = "Word inserted: '$wordInputTrimmed' Translation: '$wordInput2Trimmed'",
                            actionLabel = dismissText,
                            duration = SnackbarDuration.Short
                        )
                    }
                }
            },
            editWordConfirmButton = {
                val wordInputTrimmed = wordInput.value.trim()
                val wordInput2Trimmed = wordInput2.value.trim()

                //Ensure the inputs are not empty
                if (wordInputTrimmed.isNotEmpty() && wordInput2Trimmed.isNotEmpty()) {
                    editWordPopUp.value = false

                    //launch coroutine to edit the word and start the snackbar
                    coroutineScope.launch(Dispatchers.IO) {
                        val editedWord = getWord(wordList, wordID.value)
                        editedWord.original = wordInputTrimmed
                        editedWord.translation = wordInput2Trimmed
                        //Update the word
                        repository.updateWord(editedWord)
                        clearInput()

                        snackbarHostState.showSnackbar(
                            message = "Word updated: '$wordInputTrimmed' Translation: '$wordInput2Trimmed'",
                            actionLabel = dismissText,
                            duration = SnackbarDuration.Short
                        )
                    }
                }
            },
            editWordDeclineButton = {
                editWordPopUp.value = false
                val wordInputTrimmed = wordInput.value.trim()
                val wordInput2Trimmed = wordInput2.value.trim()

                //Launch coroutine to the delete the word and start a snackbar
                coroutineScope.launch(Dispatchers.IO) {
                    //Delete word from database
                    repository.deleteWord(getWord(wordList, wordID.value))
                    clearInput()

                    snackbarHostState.showSnackbar(
                        message = "Word removed: '$wordInputTrimmed' Translation: '$wordInput2Trimmed'",
                        actionLabel = dismissText,
                        duration = SnackbarDuration.Short
                    )
                }
            }
        )
    }
}

/**
 * The word text field for adding/editing/deleting words
 * @param wordInput First word
 * @param wordInput2 Second word
 */
@Composable
private fun WordTextField(
    wordInput: MutableState<String>,
    wordInput2: MutableState<String>
) {
    LazyColumn{
        item {
            //First text field
            OutlinedTextField(
                modifier = Modifier,
                value = wordInput.value,
                label = {
                    Text(text = stringResource(R.string.dictionary_word_input_word))
                },
                onValueChange = {
                    wordInput.value = it
                },
                singleLine = true
            )
        }

        item {
            //Second text field
            OutlinedTextField(
                modifier = Modifier,
                value = wordInput2.value,
                label = {
                    Text(text = stringResource(R.string.dictionary_word_input_translation))
                },
                onValueChange = {
                    wordInput2.value = it
                },
                singleLine = true
            )
        }
    }
}

/**
 * Content screen for the Dictionary Screen, Contains the UI elements
 * @param paddingValues The padding values of the ScaffoldComponent
 * @param originalTitle The first language title
 * @param translationTitle The second language title
 * @param wordList The database word list
 * @param addWordPopUp Display the add word popup
 * @param editWordPopUp Display the edit word popup
 * @param wordID The current selected word ID
 * @param wordInput First word text box for popups
 * @param wordInput2 Second word text box for popups
 * @param addWordConfirmButton Confirm method for add word popup
 * @param editWordConfirmButton Confirm method for edit word popup
 * @param editWordDeclineButton Decline method for edit word popup
 */
@Composable
private fun DictionaryContent(
    paddingValues: PaddingValues,
    originalTitle: String,
    translationTitle: String,
    wordList: List<Word>,
    addWordPopUp: MutableState<Boolean>,
    editWordPopUp: MutableState<Boolean>,
    wordID: MutableState<Int>,
    wordInput: MutableState<String>,
    wordInput2: MutableState<String>,
    addWordConfirmButton: () -> Unit,
    editWordConfirmButton: () -> Unit,
    editWordDeclineButton: () -> Unit
) {
    //Weighting value to display each side of the table
    val firstWordWeight = .5f
    val secondWordWeight = .5f

    LazyColumn(
        modifier=Modifier.padding(paddingValues),
        verticalArrangement = Arrangement.spacedBy(0.dp),
        userScrollEnabled = true // allow scrolling
    ){
        item {
            Row {
                //First language title
                OutlinedButton(content = {Text(text = originalTitle, fontSize=25.sp, fontWeight = FontWeight.Bold)},
                    modifier = Modifier.weight(firstWordWeight),
                    shape = CutCornerShape(0.dp),
                    onClick = {},
                    enabled = false // make inactive so its not clickable
                )

                //Second language title
                OutlinedButton(content = {Text(text = translationTitle, fontSize=25.sp, fontWeight = FontWeight.Bold)},
                    modifier = Modifier.weight(firstWordWeight),
                    shape = CutCornerShape(0.dp),
                    onClick = {},
                    enabled = false // make inactive so its not clickable
                )
            }
        }

        //Create an item for each word in the word list
        items(wordList.size) { index ->
            Row {
                //first language button
                OutlinedButton(content = {Text(text = wordList[index].original)},
                    modifier = Modifier.weight(firstWordWeight), // weight the button
                    shape = CutCornerShape(0.dp),
                    onClick = {
                        wordID.value = wordList[index].id
                        wordInput.value = wordList[index].original
                        wordInput2.value = wordList[index].translation
                        editWordPopUp.value = true // start edit word popup
                    }
                )
                //second language button
                OutlinedButton(content = {Text(text = wordList[index].translation)},
                    modifier = Modifier.weight(secondWordWeight), // weight the button
                    shape = CutCornerShape(0.dp),
                    onClick = {
                        wordID.value = wordList[index].id
                        wordInput.value = wordList[index].original
                        wordInput2.value = wordList[index].translation
                        editWordPopUp.value = true // start edit word popup
                    }
                )
            }
        }
    }

    // if floating action button selected start the add word popup
    if (addWordPopUp.value) {
        ConfirmationDialogComponent(
            stringResource(R.string.popup_dictionary_addword_title),
            {WordTextField(wordInput, wordInput2)},
            stringResource(R.string.popup_confirm),
            stringResource(R.string.popup_cancel),
            onDismissRequest = { addWordPopUp. value = false },
            confirmButton = addWordConfirmButton,
            dismissButton = { addWordPopUp. value = false }
        )
    }

    // if selecting an item in the table start the edit word popup
    if (editWordPopUp.value) {
        ConfirmationDialogComponent(
            stringResource(R.string.popup_dictionary_editword_title),
            {WordTextField(wordInput, wordInput2)},
            stringResource(R.string.popup_dictionary_editword_edit),
            stringResource(R.string.popup_dictionary_editword_delete),
            onDismissRequest = { editWordPopUp. value = false },
            confirmButton = editWordConfirmButton,
            dismissButton = editWordDeclineButton
        )
    }
}

/**
 * The Dictionary Screen preview
 */
@Preview
@Composable
fun DictionaryScreenPreview() {
    val context = LocalContext.current
    val settingsFile = stringResource(R.string.settingsFile)

    val settings by remember {
        mutableStateOf(context.getSharedPreferences(settingsFile, Context.MODE_PRIVATE))
    }

    val navController = rememberNavController()

    VocabularyTheme {
        DictionaryScreen(navController, settings)
    }
}