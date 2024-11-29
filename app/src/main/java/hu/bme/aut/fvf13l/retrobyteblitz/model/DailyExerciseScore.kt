package hu.bme.aut.fvf13l.retrobyteblitz.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "daily_exercise_scores")
data class DailyExerciseScore(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val date: String,
    val category: String,
    val score: Int
)
