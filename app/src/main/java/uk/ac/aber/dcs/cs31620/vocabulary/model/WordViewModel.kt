package uk.ac.aber.dcs.cs31620.vocabulary.model

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import uk.ac.aber.dcs.cs31620.vocabulary.datasource.VocabRepository

/**
 * The WordViewModel of the application used to get the word list from the database
 */
class WordViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: VocabRepository = VocabRepository(application)

    //Get all words
    var wordList: LiveData<List<Word>> = repository.getAllWords()
}