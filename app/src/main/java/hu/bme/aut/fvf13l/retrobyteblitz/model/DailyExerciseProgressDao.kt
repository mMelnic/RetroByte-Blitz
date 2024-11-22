package hu.bme.aut.fvf13l.retrobyteblitz.model

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface DailyExerciseProgressDao {
    @Query("SELECT * FROM daily_exercise_progress WHERE id = 1 LIMIT 1")
    suspend fun getProgress(): DailyExerciseProgress?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveProgress(progress: DailyExerciseProgress)

    @Query("DELETE FROM daily_exercise_progress WHERE id = 1")
    suspend fun clearProgress()
}