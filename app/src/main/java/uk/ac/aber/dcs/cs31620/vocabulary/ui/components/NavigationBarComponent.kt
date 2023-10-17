package uk.ac.aber.dcs.cs31620.vocabulary.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.ImportContacts
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.RestartAlt
import androidx.compose.material.icons.outlined.ImportContacts
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavHostController
import uk.ac.aber.dcs.cs31620.vocabulary.R
import uk.ac.aber.dcs.cs31620.vocabulary.ui.navigation.ScreenPath
import uk.ac.aber.dcs.cs31620.vocabulary.ui.navigation.inQuiz
import uk.ac.aber.dcs.cs31620.vocabulary.ui.navigation.navigateToScreen

/**
 * Used to determine if the current screen is the compared route
 * @param navController The current NavHostController
 * @param route The screen to be compared to
 * @return If the current screen matches with the route
 */
private fun isCurrentScreen(
    navController: NavHostController,
    route: String
): Boolean {
    return navController.currentDestination?.hierarchy?.any { it.route == route } == true
}

/**
 * Method used to check for special circumstances when leaving particular screens
 * @param navController The current NavHostController
 * @param route Destination screen heading to
 * @param leaveQuizPopUp Leave quiz popup
 * @param leaveQuizRoute The current route to leave the quiz
 */
private fun navItemClick(
    navController: NavHostController,
    route: String,
    leaveQuizPopUp: MutableState<Boolean>,
    leaveQuizRoute: MutableState<String>
) {
    if (inQuiz(navController)) {
        //Do the popup when trying to leave the quiz
        leaveQuizPopUp.value = true
        leaveQuizRoute.value = route
    } else {
        //Navigate
        navigateToScreen(navController, route, true)
    }
}

/**
 * The main navigation bar component of the application
 * @param navController The current NavHostController
 */
@Composable
fun NavigationBarComponent(
    navController: NavHostController
) {
    val resetPopUp = remember { mutableStateOf(false) }
    val leaveQuizPopUp = remember { mutableStateOf(false) }
    val leaveQuizRoute = remember { mutableStateOf("") }

    //Setup Nav Bar Items
    val navigationList = listOf(
        NavBarItem(
            focusIcon = Icons.Filled.Home,
            icon = Icons.Outlined.Home,
            text = stringResource(R.string.navbar_label_home),
            onClick = {navItemClick(navController, ScreenPath.Home.path, leaveQuizPopUp, leaveQuizRoute)},
            //Check if the current screen is the focus
            isCurrentFocus = isCurrentScreen(navController, ScreenPath.Home.path) && !resetPopUp.value
        ),
        NavBarItem(
            focusIcon = Icons.Filled.ImportContacts,
            icon = Icons.Outlined.ImportContacts,
            text = stringResource(R.string.navbar_label_dictionary),
            onClick = {navItemClick(navController, ScreenPath.Dictionary.path, leaveQuizPopUp, leaveQuizRoute)},
            //Check if the current screen is the focus
            isCurrentFocus = isCurrentScreen(navController, ScreenPath.Dictionary.path) && !resetPopUp.value
        ),
        NavBarItem(
            focusIcon = Icons.Filled.RestartAlt,
            icon = Icons.Outlined.RestartAlt,
            text = stringResource(R.string.navbar_label_reset),
            onClick = { resetPopUp.value = true },
            //Check if the reset popup is displayed as the screen focus
            isCurrentFocus = resetPopUp.value
        )
    )

    //Start the NavigationBarContent
    NavigationBarContent(navigationList, resetPopUp, leaveQuizPopUp,
        resetDialogOnClick = {
            //Start reset language
            navigateToScreen(navController, ScreenPath.ChangeLanguage.path, false)
        }, leaveQuizOnClick = {
            //Leave the current quiz
            navigateToScreen(navController, leaveQuizRoute.value, false)
        }
    )
}

/**
 * Method used to display the navigation bar UI elements
 * @param navBarItems The current items available on the NavBar
 * @param resetPopUp If the reset popup is to be displayed
 * @param leaveQuizPopUp if the leave quiz popup is to be displayed
 * @param resetDialogOnClick OnClick method for reset dialog confirmation
 * @param leaveQuizOnClick OnClick method for leave quiz confirmation
 */
@Composable
private fun NavigationBarContent(
    navBarItems: List<NavBarItem>,
    resetPopUp: MutableState<Boolean>,
    leaveQuizPopUp: MutableState<Boolean>,
    resetDialogOnClick: () -> Unit = {},
    leaveQuizOnClick: () -> Unit = {}
) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.primary
    ) {
        //Setup each navigation option
        navBarItems.forEach { navBarItem ->
            NavigationBarItem(
                icon = {
                    Icon(
                        imageVector = (if (navBarItem.isCurrentFocus) navBarItem.focusIcon else navBarItem.icon),
                        contentDescription = navBarItem.text
                    )
                },
                label = { Text(navBarItem.text) },
                selected = navBarItem.isCurrentFocus,
                onClick = navBarItem.onClick,
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor=MaterialTheme.colorScheme.secondary
                )
            )
        }
    }

    //If reset popup is being displayed
    if (resetPopUp.value) {
        //Start reset languages popup
        ConfirmationDialogComponent(
            stringResource(R.string.popup_reset_title),
            { Text(text = stringResource(R.string.popup_reset_content),
                color = MaterialTheme.colorScheme.error) },
            stringResource(R.string.popup_confirm),
            stringResource(R.string.popup_cancel),
            onDismissRequest = { resetPopUp.value = false },
            confirmButton = {
                resetPopUp.value = false
                resetDialogOnClick.invoke()
            },
            dismissButton = { resetPopUp.value = false }
        )
    }

    //If leave quiz popup is to be displayed
    if (leaveQuizPopUp.value) {
        //Start leave quiz popup
        ConfirmationDialogComponent(
            stringResource(R.string.popup_leave_quiz_title),
            { Text(text = stringResource(R.string.popup_leave_quiz_content),
                color = MaterialTheme.colorScheme.error) },
            stringResource(R.string.popup_confirm),
            stringResource(R.string.popup_cancel),
            onDismissRequest = { leaveQuizPopUp.value = false },
            confirmButton = {
                leaveQuizPopUp.value = false
                leaveQuizOnClick.invoke()
            },
            dismissButton = { leaveQuizPopUp.value = false }
        )
    }
}