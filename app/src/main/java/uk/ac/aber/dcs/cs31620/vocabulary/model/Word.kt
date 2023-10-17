package uk.ac.aber.dcs.cs31620.vocabulary.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Word Entity class, determines the structure of the database
 * @param id The ID of the word, Used as a primary key in the database and used in comparison
 * @param original The word in the users own language
 * @param translation The word in the users wanted to learn language
 */
@Entity(tableName="word_list")
data class Word(
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0,
    var original: String= "",
    var translation: String= ""
) {

}