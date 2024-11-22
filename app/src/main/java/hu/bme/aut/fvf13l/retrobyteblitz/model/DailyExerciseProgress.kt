package hu.bme.aut.fvf13l.retrobyteblitz.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "daily_exercise_progress")
data class DailyExerciseProgress(
    @PrimaryKey val id: Int = 1,  // Single row for storing current progress
    val currentGameIndex: Int,    // The current game index
    val selectedGames: String,    // JSON string of selected games
    val date: String
    //val totalScore: Int           // Total score so far
)

