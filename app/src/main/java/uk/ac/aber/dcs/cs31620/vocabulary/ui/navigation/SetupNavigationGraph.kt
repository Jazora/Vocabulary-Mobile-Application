package uk.ac.aber.dcs.cs31620.vocabulary.ui.navigation

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import uk.ac.aber.dcs.cs31620.vocabulary.R
import uk.ac.aber.dcs.cs31620.vocabulary.model.WordViewModel
import uk.ac.aber.dcs.cs31620.vocabulary.ui.crossword.CrosswordScreen
import uk.ac.aber.dcs.cs31620.vocabulary.ui.dictionary.DictionaryScreen
import uk.ac.aber.dcs.cs31620.vocabulary.ui.home.HomeScreen
import uk.ac.aber.dcs.cs31620.vocabulary.ui.languageselect.ChangeLanguageScreen
import uk.ac.aber.dcs.cs31620.vocabulary.ui.languageselect.LanguageSelectScreen
import uk.ac.aber.dcs.cs31620.vocabulary.ui.matchword.MatchWordScreen

/**
 * Used to determine if the user is in a quiz
 * @param navController the current NavHostController
 * @return if the current screen is a quiz
 */
fun inQuiz(
    navController: NavHostController
) : Boolean {
    //List of screens that are quizes
    val quizScreens = listOf(
        ScreenPath.Crossword.path,
        ScreenPath.MatchWord.path
    )

    // If the current screen is a quiz
    if (quizScreens.contains(navController.currentDestination?.route)) {
        return true
    }

    return false
}

/**
 * The main navigation function for the application, save determines
 * @param navController The current NavHostController
 * @param route Screen path wish to move to
 * @param save Is the state saved
 */
fun navigateToScreen(
    navController: NavHostController,
    route: String,
    save: Boolean
) {
    var stateSave = save

    //If the user in a quiz dont save the state
    if (inQuiz(navController)) {
        stateSave = false
    }

    //navigate to screen path
    navController.navigate(route) {
        popUpTo(navController.graph.findStartDestination().id) {
            saveState = saveState // save the states
        }
        //Only have a single screen open at one time
        launchSingleTop = true
        restoreState = stateSave

        // if save is false clear the backstack
        if (!save) {
            popUpTo(0)
        }
    }
}

/**
 * Setup the Navigation Graph and all included screens
 * @param wordViewModel The current WordViewModel
 */
@Composable
fun SetupNavigationGraph(
    wordViewModel: WordViewModel = viewModel()
) {
    //Get the current navigation controller
    val navController = rememberNavController()

    //Get the current context
    val context = LocalContext.current
    //Get the settings file path
    val settingsFile = stringResource(R.string.settingsFile)
    val settings by remember {
        //Get the settings file
        mutableStateOf(context.getSharedPreferences(settingsFile, Context.MODE_PRIVATE))
    }

    NavHost(
        navController = navController,
        startDestination = ScreenPath.Home.path // Set home as the start destination
    ) {
        //Setup all screens with their appropriate screen path route and variables
        composable(ScreenPath.Home.path) { HomeScreen(navController, settings, wordViewModel) }
        composable(ScreenPath.ChangeLanguage.path) { ChangeLanguageScreen(navController, settings) }
        composable(ScreenPath.LanguageSelect.path) { LanguageSelectScreen(navController, settings, true) }
        composable(ScreenPath.LanguageSelect2.path) { LanguageSelectScreen(navController, settings,false) }
        composable(ScreenPath.Dictionary.path) { DictionaryScreen(navController, settings, wordViewModel) }
        composable(ScreenPath.MatchWord.path) { MatchWordScreen(navController, wordViewModel) }
        composable(ScreenPath.Crossword.path) { CrosswordScreen(navController) }
    }
}