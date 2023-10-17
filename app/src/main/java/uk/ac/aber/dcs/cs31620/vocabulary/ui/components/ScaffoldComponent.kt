package uk.ac.aber.dcs.cs31620.vocabulary.ui.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController

/**
 * The main scaffold component for the application
 * @param navController The current NavHostController
 * @param floatingActionButton Floating action button to use
 * @param snackbarContent Snackbar content
 * @param snackbarHostState The snackbar host state
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScaffoldComponent(
    navController: NavHostController,
    floatingActionButton: @Composable () -> Unit = {},
    snackbarContent: @Composable (SnackbarData) -> Unit = {},
    snackbarHostState: SnackbarHostState? = null,
    content: @Composable (innerPadding: PaddingValues) -> Unit = {}
) {
    Scaffold(
        bottomBar = {
            NavigationBarComponent(navController)
        },
        content = { innerPadding ->
            content(innerPadding)
        },
        floatingActionButton = floatingActionButton,
        snackbarHost = {
            snackbarHostState?.let {
                SnackbarHost(hostState = snackbarHostState) { data ->
                    snackbarContent(data)
                }
            }
        }
    )
}