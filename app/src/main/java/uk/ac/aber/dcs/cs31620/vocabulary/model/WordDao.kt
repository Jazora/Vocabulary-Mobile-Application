package uk.ac.aber.dcs.cs31620.vocabulary.model

import androidx.lifecycle.LiveData
import androidx.room.*

/**
 * The DAO interface for interacting with the database
 */
@Dao
interface WordDao {
    //insert a word
    @Insert
    suspend fun insertWord(word: Word)

    //update a word
    @Update(onConflict= OnConflictStrategy.REPLACE)
    suspend fun updateWord(word: Word)

    //delete a word
    @Delete
    suspend fun deleteWord(word: Word)

    //delete all words
    @Query("DELETE FROM word_list")
    suspend fun deleteAllWords()

    //get all words
    @Query("SELECT * FROM word_list")
    fun getAllWords() : LiveData<List<Word>>
}