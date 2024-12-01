package hu.bme.aut.fvf13l.retrobyteblitz.model

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface DailyExerciseScoreDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveScore(score: DailyExerciseScore)

    @Query("SELECT * FROM daily_exercise_scores WHERE userId = :userId")
    suspend fun getScoresByUserId(userId: String): List<DailyExerciseScore>

    @Query(""" 
        SELECT date, COUNT(DISTINCT category) as categoryCount 
        FROM daily_exercise_scores 
        WHERE userId = :userId
        GROUP BY date
    """)
    suspend fun getCategoryCompletionCountsByUser(userId: String): List<DateCategoryCount>

}