package hu.bme.aut.fvf13l.retrobyteblitz.model

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import hu.bme.aut.fvf13l.retrobyteblitz.R
import hu.bme.aut.fvf13l.retrobyteblitz.auth.UserDatabase
import hu.bme.aut.fvf13l.retrobyteblitz.databinding.ActivityDailyExercisesBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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

        // Display today's date
        val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        binding.dateTextView.text = getString(R.string.today_date, currentDate)

        // Load progress from the database
        loadProgress { progress ->
            if (progress == null || isNewDay(progress.date, currentDate)) {
                setupNewDay(currentDate)  // Initialize a new session
            } else {
                restoreOngoingSession(progress)  // Resume the saved progress
            }
        }
    }

    private fun setupNewDay(currentDate: String) {
        // Clear previous progress
        clearProgress()

        // Randomly select one game from each category
        selectedGames = categoryGames.keys.map { category ->
            val gamesInCategory = categoryGames[category] ?: emptyList()
            gamesInCategory.random()
        }

        // Display the selected games
        binding.gamesTextView.text = selectedGames.joinToString("\n")

        // Enable the start button
        binding.startButton.isEnabled = true
        binding.startButton.text = getString(R.string.start_button_text)
        binding.startButton.setOnClickListener {
            currentGameIndex = 0
            startGamesSequentially()

            // Save the initial progress
            saveProgress()
        }
    }


    private fun restoreOngoingSession(progress: DailyExerciseProgress) {
        currentGameIndex = progress.currentGameIndex  // Resume from the last game index
        selectedGames = progress.selectedGames.split(",")  // Convert CSV back to a list

        // Display the selected games
        binding.gamesTextView.text = selectedGames.joinToString("\n")

        // Enable the start button to resume
        binding.startButton.isEnabled = true
        binding.startButton.setOnClickListener {
            startGamesSequentially()  // Resume the game sequence
        }
    }


    override fun onPause() {
        super.onPause()
        saveProgress()
    }

    private fun startGamesSequentially() {
        if (selectedGames.isNotEmpty()) {
            runNextGame()
        }
    }

    private fun runNextGame() {
        if (currentGameIndex < selectedGames.size) {
            val nextGame = selectedGames[currentGameIndex]
            showInstructionsDialog(nextGame) {
                launchGame(nextGame)
            }
        } else {
            // All games completed
            Toast.makeText(this, R.string.daily_exercises_completed, Toast.LENGTH_SHORT).show()
            binding.startButton.isEnabled = false
            binding.startButton.text = getString(R.string.completed_today)
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
            saveProgress()
            runNextGame()  // Proceed to the next game
        }
    }

    private fun isNewDay(savedDate: String, currentDate: String): Boolean {
        return savedDate != currentDate
    }

    // Save the current date in SharedPreferences
    private fun saveLastCompletedDate() {
        val sharedPref = getSharedPreferences("user_prefs", MODE_PRIVATE)
        val editor = sharedPref.edit()
        val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        editor.putString("last_completed_date", currentDate)
        editor.apply()
    }

    private fun saveProgress() {
        val db = UserDatabase.getDatabase(this)  // Get the database instance
        val progressDao = db.dailyExerciseProgressDao()
        val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

        val progress = DailyExerciseProgress(
            id = 1,
            currentGameIndex = currentGameIndex,
            selectedGames = selectedGames.joinToString(","), // Store as CSV or JSON
            date = currentDate
            //totalScore = 0 // TODO logic for score
        )

        // Save progress using a background thread
        CoroutineScope(Dispatchers.IO).launch {
            progressDao.saveProgress(progress)
        }
    }

    private fun loadProgress(onLoaded: (DailyExerciseProgress?) -> Unit) {
        val db = UserDatabase.getDatabase(this)
        val progressDao = db.dailyExerciseProgressDao()

        CoroutineScope(Dispatchers.IO).launch {
            val progress = progressDao.getProgress() // Fetch the progress
            withContext(Dispatchers.Main) {
                onLoaded(progress) // Return the progress to the caller
            }
        }
    }

    private fun clearProgress() {
        val db = UserDatabase.getDatabase(this)
        val progressDao = db.dailyExerciseProgressDao()

        CoroutineScope(Dispatchers.IO).launch {
            progressDao.clearProgress()
        }
    }

    companion object {
        private const val GAME_REQUEST_CODE = 1001
    }
}
