package hu.bme.aut.fvf13l.retrobyteblitz.auth

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import hu.bme.aut.fvf13l.retrobyteblitz.model.DailyExerciseProgress
import hu.bme.aut.fvf13l.retrobyteblitz.model.DailyExerciseProgressDao

@Database(entities = [User::class, DailyExerciseProgress::class], version = 2)
abstract class UserDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun dailyExerciseProgressDao(): DailyExerciseProgressDao

    companion object {
        @Volatile
        private var INSTANCE: UserDatabase? = null

        fun getDatabase(context: Context): UserDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    UserDatabase::class.java,
                    "retrobyte_db"
                )
                    .fallbackToDestructiveMigration() // Handle version changes
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
