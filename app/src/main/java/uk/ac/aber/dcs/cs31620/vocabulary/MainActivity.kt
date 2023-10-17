package uk.ac.aber.dcs.cs31620.vocabulary

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import uk.ac.aber.dcs.cs31620.vocabulary.ui.navigation.SetupNavigationGraph
import uk.ac.aber.dcs.cs31620.vocabulary.ui.theme.VocabularyTheme

/**
 * The main entry point into the application
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            VocabularyTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    SetupNavigationGraph() // load the navigation graph
                }
            }
        }
    }
}

/**
 * Main activity preview
 */
@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    VocabularyTheme {
        SetupNavigationGraph()
    }
}