package uk.ac.aber.dcs.cs31620.vocabulary.datasource

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import uk.ac.aber.dcs.cs31620.vocabulary.R
import uk.ac.aber.dcs.cs31620.vocabulary.model.Word
import uk.ac.aber.dcs.cs31620.vocabulary.model.WordDao

/**
 * Class used to setup the original creation of the database and returning the database
 */
@Database(entities = [Word::class], version = 1)
abstract class VocabRoomDatabase : RoomDatabase() {
    abstract fun wordDao(): WordDao

    companion object {
        private var instance: VocabRoomDatabase? = null

        // Get the database in a multi-threaded friendly way
        @Synchronized
        fun getDatabase(context: Context): VocabRoomDatabase? {
            if (instance == null) {
                instance =
                    Room.databaseBuilder<VocabRoomDatabase>(
                        context.applicationContext,
                        VocabRoomDatabase::class.java,
                        context.getString(R.string.database_name)
                    ).allowMainThreadQueries().build()
            }
            return instance
        }
    }
}