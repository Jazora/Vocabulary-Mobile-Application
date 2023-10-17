package uk.ac.aber.dcs.cs31620.vocabulary.datasource

import android.app.Application
import uk.ac.aber.dcs.cs31620.vocabulary.model.Word

/**
 * Class used to interact with the WordDao on how to interface with the database
 */
class VocabRepository(application: Application) {
    //Get the database and interact through the word dao
    private val wordDao = VocabRoomDatabase.getDatabase(application)!!.wordDao()

    //Coroutine friendly way of inserting words
    suspend fun insertWord(word: Word) {
        wordDao.insertWord(word)
    }

    //Coroutine friendly way of updating words
    suspend fun updateWord(word: Word) {
        wordDao.updateWord(word)
    }

    //Coroutine friendly way of deleting words
    suspend fun deleteWord(word: Word) {
        wordDao.deleteWord(word)
    }

    //Coroutine friendly way of deleting all words
    suspend fun deleteAllWords() {
        wordDao.deleteAllWords()
    }

    //Get all words
    fun getAllWords() = wordDao.getAllWords()
}