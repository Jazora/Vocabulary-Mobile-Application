package uk.ac.aber.dcs.cs31620.vocabulary.ui.languageselect

/**
 * Common language data type
 * @param language The language name
 * @param flag The resource ID for the flag
 */
data class CommonLanguage(
    val language: String,
    val flag: Int
)