package hu.bme.aut.fvf13l.retrobyteblitz.model

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import hu.bme.aut.fvf13l.retrobyteblitz.R
import hu.bme.aut.fvf13l.retrobyteblitz.auth.UserDatabase
import hu.bme.aut.fvf13l.retrobyteblitz.databinding.ActivityDailyExercisesBinding
import hu.bme.aut.fvf13l.retrobyteblitz.utility.SessionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DailyExercisesActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDailyExercisesBinding
    private lateinit var database: DatabaseReference

    private val categoryGames = mapOf(
        "Logic" to listOf("Number Of", "Sudoku", "Slider"),
        "Memory" to listOf("Colors", "Grid", "Card"),
        "Calculation" to listOf("Calculation", "Sequence", "Moving Sum"),
        "Visual" to listOf("Descending", "Stroop", "RomanNum")
    )

    private var currentGameIndex = 0
    private lateinit var selectedGames: List<String>
    private lateinit var currentDate: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        database = Firebase.database.reference

        binding = ActivityDailyExercisesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        binding.dateTextView.text = getString(R.string.today_date, currentDate)

        loadProgress { progress ->
            if (progress == null || isNewDay(progress.date, currentDate)) {
                setupNewDay()
            } else {
                restoreOngoingSession(progress)
            }
        }
    }

    private fun setupNewDay() {
        clearProgress()

        selectedGames = categoryGames.keys.map { category ->
            val gamesInCategory = categoryGames[category] ?: emptyList()
            gamesInCategory.random()
        }
        binding.gamesTextView.text = selectedGames.joinToString("\n")

        binding.startButton.isEnabled = true
        binding.startButton.text = getString(R.string.start_button_text)
        binding.startButton.setOnClickListener {
            currentGameIndex = 0
            startGamesSequentially()
            saveProgress()
        }
    }


    private fun restoreOngoingSession(progress: DailyExerciseProgress) {
        currentGameIndex = progress.currentGameIndex
        selectedGames = progress.selectedGames.split(",")

        binding.gamesTextView.text = selectedGames.joinToString("\n")

        binding.startButton.isEnabled = true
        binding.startButton.setOnClickListener {
            startGamesSequentially()
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
            putExtra("TIME_LIMIT", 60000L)
        }
        startActivityForResult(intent, GAME_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == GAME_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            val score = data?.getIntExtra("SCORE", 0) ?: 0
            val currentGame = selectedGames[currentGameIndex]
            val username = SessionManager.getUsername(this)

            val category = categoryGames.entries.firstOrNull { (_, games) ->
                games.contains(currentGame)
            }?.key ?: "Unknown"

            val db = UserDatabase.getDatabase(this)
            CoroutineScope(Dispatchers.IO).launch {
                db.dailyExerciseScoreDao().saveScore(
                    DailyExerciseScore(
                        date = currentDate,
                        category = category,
                        score = score
                    )
                )
            }
            val dailyPath = "leaderboard/$currentDate/$username"
            database.child(dailyPath).get().addOnSuccessListener {
                val existingScore = it.value as? Long ?: 0
                database.child(dailyPath).setValue(existingScore + score)
            }

            currentGameIndex++
            saveProgress()
            runNextGame()
        }
    }

    private fun isNewDay(savedDate: String, currentDate: String): Boolean {
        return savedDate != currentDate
    }

    private fun saveProgress() {
        val db = UserDatabase.getDatabase(this)
        val progressDao = db.dailyExerciseProgressDao()

        val progress = DailyExerciseProgress(
            id = 1,
            currentGameIndex = currentGameIndex,
            selectedGames = selectedGames.joinToString(","),
            date = currentDate
        )

        CoroutineScope(Dispatchers.IO).launch {
            progressDao.saveProgress(progress)
        }
    }

    private fun loadProgress(onLoaded: (DailyExerciseProgress?) -> Unit) {
        val db = UserDatabase.getDatabase(this)
        val progressDao = db.dailyExerciseProgressDao()

        CoroutineScope(Dispatchers.IO).launch {
            val progress = progressDao.getProgress()
            withContext(Dispatchers.Main) {
                onLoaded(progress)
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
