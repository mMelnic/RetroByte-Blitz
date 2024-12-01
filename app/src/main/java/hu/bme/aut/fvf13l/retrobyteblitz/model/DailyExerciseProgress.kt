package hu.bme.aut.fvf13l.retrobyteblitz.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "daily_exercise_progress")
data class DailyExerciseProgress(
    @PrimaryKey val userId: String,
    val currentGameIndex: Int,
    val selectedGames: String,
    val date: String
)