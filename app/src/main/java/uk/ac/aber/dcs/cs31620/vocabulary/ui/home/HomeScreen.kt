package uk.ac.aber.dcs.cs31620.vocabulary.ui.home

import android.annotation.SuppressLint
import android.content.SharedPreferences
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import uk.ac.aber.dcs.cs31620.vocabulary.R
import uk.ac.aber.dcs.cs31620.vocabulary.datasource.VocabRoomDatabase
import uk.ac.aber.dcs.cs31620.vocabulary.model.Word
import uk.ac.aber.dcs.cs31620.vocabulary.model.WordViewModel
import uk.ac.aber.dcs.cs31620.vocabulary.ui.components.ConditionButtonComponent
import uk.ac.aber.dcs.cs31620.vocabulary.ui.components.ConfirmationDialogComponent
import uk.ac.aber.dcs.cs31620.vocabulary.ui.components.ScaffoldComponent
import uk.ac.aber.dcs.cs31620.vocabulary.ui.crossword.MatchingWord
import uk.ac.aber.dcs.cs31620.vocabulary.ui.crossword.canCrossword
import uk.ac.aber.dcs.cs31620.vocabulary.ui.matchword.canMatchWord
import uk.ac.aber.dcs.cs31620.vocabulary.ui.navigation.ScreenPath
import uk.ac.aber.dcs.cs31620.vocabulary.ui.navigation.navigateToScreen
import uk.ac.aber.dcs.cs31620.vocabulary.ui.theme.VocabularyTheme

/**
 * The entry point for HomeScreen, Sets up for HomeScreenContent
 * @param navController The current NavHostController
 * @param settings The configuration file
 * @param wordViewModel The current WordViewModel
 */
@Composable
fun HomeScreen(
    navController: NavHostController,
    settings: SharedPreferences,
    wordViewModel: WordViewModel = viewModel()
) {
    val firstLanguage = stringResource(R.string.firstLanguage)
    val secondLanguage = stringResource(R.string.secondLanguage)
    val emptyString = stringResource(R.string.emptyString)

    //Get the first language setting
    val original by remember {
        mutableStateOf(settings.getString(firstLanguage, emptyString))
    }

    //Get the second language setting
    val translation by remember {
        mutableStateOf(settings.getString(secondLanguage, emptyString))
    }

    //If either of the language settings are invalid send the user to the language select screen
    if (original == emptyString || translation == emptyString) { // first time loading up the app
        val context = LocalContext.current.applicationContext
        // create an empty database
        VocabRoomDatabase.getDatabase(context)
        // Navigate to the language select screen
        navigateToScreen(navController, ScreenPath.LanguageSelect.path, false)
    }
    else {
        ScaffoldComponent(
            navController = navController,
        ) {
            //Get the word list
            val wordList by wordViewModel.wordList.observeAsState(listOf())

            //Is the crossword available
            val crosswordAvailable = remember { mutableStateOf(false) }
            val coroutineScope = rememberCoroutineScope()

            //Start the coroutine to check if crossword is available
            LaunchedEffect(Unit) {
                coroutineScope.launch(Dispatchers.Main) {
                    canCrossword(wordList, crosswordAvailable, mutableStateOf(Word()),
                        mutableStateOf(MatchingWord()), mutableStateOf(MatchingWord()))
                }
            }

            //Notification settings used for alerting the user to error messages
            val notificationPopUp = remember { mutableStateOf(false) }
            val notificationContent = remember { mutableStateOf("") }

            //Error messages on why some quizes are unavailable
            val crosswordErrorMsg = stringResource(R.string.homepage_crossword_error_msg)
            val matchwordErrorMsg = stringResource(R.string.homepage_matchword_error_msg)

            //Start home screen content
            HomeScreenContent(it, crosswordAvailable.value, canMatchWord(wordList), notificationPopUp, notificationContent.value,
                crosswordOnClick={
                    // navigate to crossword screen
                    navigateToScreen(navController, ScreenPath.Crossword.path, true)
                },
                matchWordOnClick = {
                    //navigate to match word screen
                    navigateToScreen(navController, ScreenPath.MatchWord.path, true)
                },
                crosswordOnClickFailed = {
                    // Display crossword error message
                    notificationPopUp.value = true
                    notificationContent.value = crosswordErrorMsg
                },
                matchWordOnClickFailed = {
                    // Display match word error message
                    notificationPopUp.value = true
                    notificationContent.value = matchwordErrorMsg
                }

            )
        }
    }
}

/**
 * The Home Screen content, Contained the UI elements of the home screen
 * @param paddingValues The padding values for Scaffold component
 * @param canCrossword Can the user access the crossword quiz
 * @param canMatchWord Can the user access the match word quiz
 * @param notificationPopUp If to display a error message
 * @param notificationContent The content for the error message
 * @param crosswordOnClick If the user can crossword onclick method
 * @param crosswordOnClickFailed if the user cannot crossword onclick method
 * @param matchWordOnClick If the user can match word onclick method
 * @param matchWordOnClickFailed if the user cannot match word onclick method
 */
@Composable
private fun HomeScreenContent(
    paddingValues: PaddingValues,
    canCrossword: Boolean = false,
    canMatchWord: Boolean = false,
    notificationPopUp: MutableState<Boolean>,
    notificationContent: String = "",
    crosswordOnClick: () -> Unit = {},
    crosswordOnClickFailed: () -> Unit = {},
    matchWordOnClick: () -> Unit = {},
    matchWordOnClickFailed: () -> Unit  = {}
){
    Box(
        modifier = Modifier.padding(paddingValues).fillMaxSize()
    ) {
        LazyColumn(
            Modifier.align(Alignment.Center),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ){
            //Crossword Button
            item {
                ConditionButtonComponent(
                    modifier = Modifier.height(80.dp).width(300.dp),
                    text = stringResource(R.string.home_crossword_button),
                    fontSize=25.sp,
                    condition = canCrossword,
                    onClick = crosswordOnClick,
                    onClickFailed = crosswordOnClickFailed
                )
            }

            //Match Word Button
            item {
                ConditionButtonComponent(
                    modifier = Modifier.height(80.dp).width(300.dp),
                    text = stringResource(R.string.home_matchword_button),
                    fontSize=25.sp,
                    condition = canMatchWord,
                    onClick = matchWordOnClick,
                    onClickFailed = matchWordOnClickFailed
                )
            }
        }

        //Notification of error messages
        if (notificationPopUp.value) {
            ConfirmationDialogComponent(
                stringResource(R.string.popup_home_notification_title),
                { Text(text = notificationContent,
                    color = MaterialTheme.colorScheme.error) },
                stringResource(R.string.popup_confirm),
                "",
                onDismissRequest = { notificationPopUp.value = false },
                dismissButton = { notificationPopUp.value = false },
                confirmButton = { notificationPopUp.value = false }
            )
        }
    }
}

/**
 * The home screen preview
 */
@SuppressLint("UnrememberedMutableState")
@Preview
@Composable
fun HomeScreenPreview() {
    val navController = rememberNavController()

    VocabularyTheme {
        ScaffoldComponent(
            navController = navController,
        ) { innerPadding ->
            Surface(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
            ) {
                HomeScreenContent(paddingValues = innerPadding, notificationPopUp = mutableStateOf(false))
            }
        }
    }
}