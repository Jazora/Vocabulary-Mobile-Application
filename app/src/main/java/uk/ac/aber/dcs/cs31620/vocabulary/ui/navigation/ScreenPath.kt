package uk.ac.aber.dcs.cs31620.vocabulary.ui.navigation

/**
 * Each of the screen path objects, used within SetupNavigationGraph
 * @param path Screen path route
 */
sealed class ScreenPath(
    val path: String
    ) {
    object Home : ScreenPath("home") // Home page
    object ChangeLanguage : ScreenPath("changeLanguage") // Changing of language page
    object LanguageSelect : ScreenPath("language") // First langauge selection page
    object LanguageSelect2 : ScreenPath("language2") // Second langauge selection page
    object Dictionary : ScreenPath("dictionary") // Dictionary
    object MatchWord : ScreenPath("matchword") // MatchWord quiz
    object Crossword : ScreenPath("crossword") // Crossword quiz
}