package uk.ac.aber.dcs.cs31620.vocabulary.ui.components

import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Data class for each navigation bar items
 * @param focusIcon If navigation option is the current focus display this icon
 * @param icon if the navigation option is not the current focus display this icon
 * @param text The text under the navigation item
 * @param onClick The OnClick method for the navigation item
 * @param isCurrentFocus Boolean to determine if this is the current focus
 */
data class NavBarItem(
    val focusIcon: ImageVector,
    val icon: ImageVector,
    val text: String,
    val onClick: () -> Unit = {},
    val isCurrentFocus: Boolean
) {
}