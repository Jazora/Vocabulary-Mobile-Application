package uk.ac.aber.dcs.cs31620.vocabulary.ui.languageselect

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import uk.ac.aber.dcs.cs31620.vocabulary.R
import uk.ac.aber.dcs.cs31620.vocabulary.datasource.VocabRepository
import uk.ac.aber.dcs.cs31620.vocabulary.ui.navigation.ScreenPath
import uk.ac.aber.dcs.cs31620.vocabulary.ui.navigation.navigateToScreen
import uk.ac.aber.dcs.cs31620.vocabulary.ui.theme.VocabularyTheme

/**
 * This method is used for changing configuration settings
 * @param settings The settings file
 * @param settingID The settings ID
 * @param value The value for the setting ID
 */
private fun changeLanguageSetting(
    settings: SharedPreferences,
    settingID: String,
    value: String
) {
    val editor = settings.edit()
    editor.putString(settingID, value.trim()) // Trim value
    editor.apply() // apply changes
}

/**
 * The entry point for when reseting the langauge has selected
 * @param navController The current NavHostController
 * @param settings The current settings file
 */
@Composable
fun ChangeLanguageScreen(
    navController: NavHostController,
    settings: SharedPreferences
){
    //remove settings and words from the database
    val context = LocalContext.current.applicationContext
    // get the repository
    val repository = VocabRepository(context as Application)
    val firstLanguage = stringResource(R.string.firstLanguage)
    val secondLanguage = stringResource(R.string.secondLanguage)
    val emptyString = stringResource(R.string.emptyString)

    LaunchedEffect(Unit){
        changeLanguageSetting(settings, firstLanguage, emptyString) // clear the config file
        changeLanguageSetting(settings, secondLanguage, emptyString)
        repository.deleteAllWords() // Remove all words from the database
    }

    LanguageSelectScreen(navController, settings, true)
}

/**
 * The entry point for Language Select screen, Contains variables for the content,
 * can be switched between the first and second language
 * @param navController The current NavHostController
 * @param settings The current settings file
 * @param firstPage Is the current page the first
 */
@Composable
fun LanguageSelectScreen(
    navController: NavHostController,
    settings: SharedPreferences,
    firstPage: Boolean
) {
    //The language selected
    val inputValue = rememberSaveable { ( mutableStateOf("")) }
    val firstLanguage = stringResource(R.string.firstLanguage)
    val secondLanguage = stringResource(R.string.secondLanguage)

    //List of common languages setup
    val common_languages = listOf(
        CommonLanguage(stringResource(R.string.common_english), R.drawable.flag_british),
        CommonLanguage(stringResource(R.string.common_french), R.drawable.flag_french),
        CommonLanguage(stringResource(R.string.common_german), R.drawable.flag_german),
        CommonLanguage(stringResource(R.string.common_spanish), R.drawable.flag_spanish),
        CommonLanguage(stringResource(R.string.common_swedish), R.drawable.flag_swedish),
        CommonLanguage(stringResource(R.string.common_danish), R.drawable.flag_danish)
    )

    if (firstPage) { // First language select page
        LanguageSelectContent(
                    stringResource(R.string.language_choose_your_language), inputValue, common_languages,
            buttonClicked = {
                //Update the configuration file
                changeLanguageSetting(settings, firstLanguage, inputValue.value)
                //Navigate to the second screen
                navigateToScreen(navController, ScreenPath.LanguageSelect2.path, true)
            },
            onValueChanged = {
                inputValue.value = it
            }
        )
    }
    else { // Second language select page
        LanguageSelectContent(
                    stringResource(R.string.language_choose_learn_language), inputValue, common_languages,
            buttonClicked = {
                //Update second language in configuration file
                changeLanguageSetting(settings, secondLanguage, inputValue.value)
                //Navigate to homepage
                navigateToScreen(navController, ScreenPath.Home.path, false)
            },
            onValueChanged = {
                inputValue.value = it
            }
        )
    }
}

/**
 * The language scaffold used for the floating action button
 * @param floatingActionButton The floating action button for the saffold
 * @param content Content of the screen
 */
@Composable
private fun LanguageScaffold (
    floatingActionButton: @Composable () -> Unit = {},
    content: @Composable (innerPadding: PaddingValues) -> Unit = {}
) {
    Scaffold(
        content = { innerPadding ->
            content(innerPadding)
        },
        floatingActionButton = floatingActionButton,
    )
}

/**
 * Common Language button used for quick language selection
 * @param text The buttons text
 * @param The image of the button
 * @param inputValue The language text box that will be filled on selection
 */
@Composable
private fun CommonLanguageButton(
    text: String,
    @DrawableRes imageResource: Int,
    inputValue: MutableState<String>,
) {
    Column(Modifier.fillMaxSize()) {
        Image(
            modifier = Modifier
                .fillMaxSize()
                .clickable { inputValue.value = text },
            painter = painterResource(imageResource),
            contentDescription = stringResource(R.string.app_name),
            contentScale = ContentScale.Crop
        )
        Text(modifier = Modifier.align(Alignment.CenterHorizontally), text = text, fontWeight = FontWeight.SemiBold)
    }
}

/**
 * The content of the Language select screen, Contains the UI elements
 * @param inputTitle The title of the page
 * @param inputValue The text box that will be filled
 * @param common_languages The common languages available
 * @param onValueChanged When the text box is updated
 * @param buttonClicked When the floating action button is clicked
 */
@Composable
private fun LanguageSelectContent(
    inputTitle: String,
    inputValue: MutableState<String>,
    common_languages: List<CommonLanguage>,
    onValueChanged: (String) -> Unit = {},
    buttonClicked: () -> Unit = {}
) {
    LanguageScaffold(
        //Setup the floating action button
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    //Dont move screen until valid entry of language
                    if (inputValue.value.isNotEmpty()) {
                        buttonClicked.invoke()
                    }
            },
                //Change colour when its valid
                containerColor = (if (inputValue.value.isNotEmpty()) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.error)
            ) {
                Icon(
                    imageVector = Icons.Filled.ArrowForward,
                    contentDescription = stringResource(R.string.language_select_continue),
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    ) {
        Column(
            Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ){
            //Text field that will be filled for langauge selection
            OutlinedTextField(
                modifier = Modifier.align(Alignment.CenterHorizontally),
                value = inputValue.value,
                label = { Text(text = inputTitle) },
                onValueChange = onValueChanged,
                singleLine = true
            )

            Spacer(Modifier.height(10.dp))

            //Title of common choices
            Text(text = stringResource(R.string.language_common_choices), fontSize = 20.sp, fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Start, modifier = Modifier.fillMaxWidth())

            //Grid layout of common choices with flag icons
            LazyVerticalGrid(
                modifier = Modifier
                    .padding(vertical = 10.dp)
                    .align(Alignment.CenterHorizontally),
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ){
                itemsIndexed(common_languages) { i, language ->
                    CommonLanguageButton(language.language, language.flag, inputValue)
                }
            }
        }
    }
}

/**
 * Language Screen Preview
 */
@Preview
@Composable
fun LanguageSelectPreview() {
    val context = LocalContext.current
    val settingsFile = stringResource(R.string.settingsFile)
    val settings by remember {
        mutableStateOf(context.getSharedPreferences(settingsFile, Context.MODE_PRIVATE))
    }

    val navController = rememberNavController()

    VocabularyTheme {
        LanguageSelectScreen(navController, settings,true)
    }
}