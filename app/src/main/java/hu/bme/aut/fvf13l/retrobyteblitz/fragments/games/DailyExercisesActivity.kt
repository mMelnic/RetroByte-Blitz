package hu.bme.aut.fvf13l.retrobyteblitz.fragments.games

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import hu.bme.aut.fvf13l.retrobyteblitz.R
import hu.bme.aut.fvf13l.retrobyteblitz.databinding.ActivityDailyExercisesBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DailyExercisesActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDailyExercisesBinding

    private val categoryGames = mapOf(
        "Logic" to listOf("Number Of", "Sudoku", "Slider"),
        "Memory" to listOf("Colors", "Grid", "Card"),
        "Calculation" to listOf("Calculation", "Sequence", "Moving Sum"),
        "Visual" to listOf("Descending", "Stroop", "RomanNum")
    )

    private var currentGameIndex = 0
    private lateinit var selectedGames: List<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize View Binding
        binding = ActivityDailyExercisesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Get current date
        val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        binding.dateTextView.text = getString(R.string.today_date, currentDate)

        // Randomly select one game from each category
        selectedGames = categoryGames.keys.map { category ->
            val gamesInCategory = categoryGames[category] ?: emptyList()
            gamesInCategory.random()  // Randomly select one game per category
        }

        // Display selected games
        binding.gamesTextView.text = selectedGames.joinToString("\n")

        // Start button click listener
        binding.startButton.setOnClickListener {
            startGamesSequentially()
        }
    }

    private fun startGamesSequentially() {
        if (selectedGames.isNotEmpty()) {
            runNextGame()
        }
    }

    private fun runNextGame() {
        if (currentGameIndex < selectedGames.size) {
            val nextGame = selectedGames[currentGameIndex]
            // Show instructions for the game
            showInstructionsDialog(nextGame) {
                launchGame(nextGame)
            }
        } else {
            // All games completed
            Toast.makeText(this, R.string.daily_exercises_completed, Toast.LENGTH_SHORT).show()
        }
    }

    private fun showInstructionsDialog(gameName: String, onDismiss: () -> Unit) {
        val instructions = getGameInstructions(gameName)
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.instructions_title, gameName))
            .setMessage(instructions)
            .setPositiveButton(getString(R.string.start_button)) { _, _ -> onDismiss() }
            .show()
    }

    private fun getGameInstructions(gameName: String): String {
        return when (gameName) {
            "Sudoku" -> getString(R.string.sudoku_instructions)
            "Calculation" -> getString(R.string.calculation_instructions)
            "Colors" -> getString(R.string.colors_instructions)
            "Grid" -> getString(R.string.grid_instructions)
            "Sequence" -> getString(R.string.sequence_instructions)
            "Number Of" -> getString(R.string.number_of_instructions)
            "Moving Sum" -> getString(R.string.moving_sum_instructions)
            "Descending" -> getString(R.string.descending_instructions)
            "Card" -> getString(R.string.card_instructions)
            "Slider" -> getString(R.string.slider_instructions)
            "Stroop" -> getString(R.string.stroop_instructions)
            else -> getString(R.string.no_instructions)
        }
    }

    private fun launchGame(gameName: String) {
        val intent = Intent(this, GameActivity::class.java).apply {
            putExtra("GAME_NAME", gameName)
            putExtra("TIME_LIMIT", 60000L)  // Adjust the time limit as needed
        }
        startActivityForResult(intent, GAME_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == GAME_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            // After a game is finished, move to the next game
            currentGameIndex++
            runNextGame()  // Proceed to the next game
        }
    }

    companion object {
        private const val GAME_REQUEST_CODE = 1001
    }
}
