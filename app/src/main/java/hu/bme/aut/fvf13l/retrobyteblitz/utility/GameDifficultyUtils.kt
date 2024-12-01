package hu.bme.aut.fvf13l.retrobyteblitz.utility

object GameDifficultyUtils {

    private const val MULTIPLIER = 100
    private val gameDifficultyMap = mapOf(
        "Sudoku" to 15,
        "Calculation" to 3,
        "Colors" to 3,
        "Grid" to 3,
        "Sequence" to 5,
        "Number Of" to 2,
        "Moving Sum" to 3,
        "Descending" to 2,
        "Card" to 5,
        "Slider" to 3,
        "Stroop" to 1,
        "Roman Gladiator" to 3
    )

    private fun getDifficultyLevel(gameName: String): Int {
        return gameDifficultyMap[gameName] ?: 1 // Default difficulty level 1 if not found
    }

    fun calculateScore(gameName: String, successfulRounds: Int): Int {
        val difficultyLevel = getDifficultyLevel(gameName)
        return successfulRounds * difficultyLevel * MULTIPLIER
    }
}
