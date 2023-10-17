package uk.ac.aber.dcs.cs31620.vocabulary.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * This button type is used for determining conditions when a button is selected and will display
 * appropriate button colours when condition is true or false
 * @param modifier The modifier of the button
 * @param text The text of the button
 * @param fontSize The font size of the button
 * @param condition Boolean to determine condition
 * @param onClick When clicked on condition true
 * @param onClickFailed When clicked on condition false
 */
@Composable
fun ConditionButtonComponent(
    modifier: Modifier = Modifier,
    text: String,
    fontSize: TextUnit = 15.sp,
    condition: Boolean,
    onClick: () -> Unit = {},
    onClickFailed: () -> Unit = {}
){
    OutlinedButton(
        modifier = modifier,
        shape= RoundedCornerShape(20),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onPrimary),
        onClick = { if (condition) onClick.invoke() else onClickFailed.invoke()},
        colors = ButtonDefaults.buttonColors( // Colour button based on condition
                containerColor = (if (condition) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.error)
        )
    ){
        Text(text=text, fontSize = fontSize)
    }
}