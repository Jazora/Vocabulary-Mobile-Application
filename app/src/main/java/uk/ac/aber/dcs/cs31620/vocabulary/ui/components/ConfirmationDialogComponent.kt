package uk.ac.aber.dcs.cs31620.vocabulary.ui.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable

/**
 * Main Dialog component used within the application, allowing for easy changes in the future
 * @param title Title of the popup
 * @param content Content of the popup
 * @param confirmText Confirmation text
 * @param dismissText Dismiss text
 * @param onDismissRequest When popup is dismissed
 * @param confirmButton When confirmation button is pressed
 * @param dismissButton When the dismiss button is pressed
 */
@Composable
fun ConfirmationDialogComponent(
    title: String,
    content: (@Composable () -> Unit)? = null,
    confirmText: String,
    dismissText: String,
    onDismissRequest: () -> Unit,
    confirmButton: () -> Unit,
    dismissButton: () -> Unit = {}
){
    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text(title) },
        text = content,
        confirmButton = {
            TextButton(
                onClick = confirmButton
            ) {
                Text(text = confirmText)
            }
        },
        dismissButton = {
            TextButton(
                onClick = dismissButton
            ) {
                Text(text = dismissText)
            }
        }
    )
}