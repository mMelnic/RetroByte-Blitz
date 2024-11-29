package hu.bme.aut.fvf13l.retrobyteblitz.model

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface DailyExerciseScoreDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveScore(score: DailyExerciseScore)

    @Query("SELECT * FROM daily_exercise_scores")
    suspend fun getAllScores(): List<DailyExerciseScore>
}