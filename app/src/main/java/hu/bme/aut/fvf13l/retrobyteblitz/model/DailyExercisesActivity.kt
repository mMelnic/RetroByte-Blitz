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
        "Visual" to listOf("Descending", "Stroop", "Roman Numerals")
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
        val dateKey = currentDate

        database.child("daily_games").child(dateKey).get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                val storedGames = snapshot.child("games").value as? String
                if (storedGames != null) {
                    selectedGames = storedGames.split(",")
                    binding.gamesTextView.text = selectedGames.joinToString("\n")
                }
            } else {
                selectedGames = categoryGames.keys.map { category ->
                    val gamesInCategory = categoryGames[category] ?: emptyList()
                    gamesInCategory.random()
                }

                database.child("daily_games").child(dateKey).setValue(
                    mapOf("games" to selectedGames.joinToString(","))
                )

                binding.gamesTextView.text = selectedGames.joinToString("\n")
            }

            binding.startButton.isEnabled = true
            binding.startButton.text = getString(R.string.start_button_text)
            binding.startButton.setOnClickListener {
                currentGameIndex = 0
                startGamesSequentially()
                saveProgress()
            }
        }.addOnFailureListener {
            Toast.makeText(this, R.string.error_fetching_games, Toast.LENGTH_SHORT).show()
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
            "Roman Numerals" -> getString(R.string.roman_num_instructions)
            else -> getString(R.string.no_instructions)
        }
    }

    private fun launchGame(gameName: String) {
        val timeLimit = if (gameName == "Sudoku") 600000L else 60000L // 10 minutes for Sudoku, 1 minute for others

        val intent = Intent(this, GameActivity::class.java).apply {
            putExtra("GAME_NAME", gameName)
            putExtra("TIME_LIMIT", timeLimit)
        }

        startActivityForResult(intent, GAME_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == GAME_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            val score = data?.getIntExtra("SCORE", 0) ?: 0
            val currentGame = selectedGames[currentGameIndex]
            val userId = SessionManager.getUserId(this) ?: return

            val category = categoryGames.entries.firstOrNull { (_, games) ->
                games.contains(currentGame)
            }?.key ?: "Unknown"

            val db = UserDatabase.getDatabase(this)
            CoroutineScope(Dispatchers.IO).launch {
                db.dailyExerciseScoreDao().saveScore(
                    DailyExerciseScore(
                        date = currentDate,
                        userId = userId,
                        category = category,
                        score = score
                    )
                )
            }
            updateLeaderboard(score)

            currentGameIndex++
            saveProgress()
            runNextGame()
        }
    }

    private fun updateLeaderboard(score: Int) {
        val userId = SessionManager.getUserId(this)
        val username = SessionManager.getUsername(this)

        if (userId == null || username == null) {
            Toast.makeText(this, "User session invalid. Please log in again.", Toast.LENGTH_SHORT).show()
            return
        }
        val dailyPath = "leaderboard/$currentDate/$userId"

        database.child(dailyPath).get().addOnSuccessListener { snapshot ->
            val existingData = snapshot.value as? Map<*, *>
            val existingScore = (existingData?.get("score") as? Long) ?: 0
            val updatedScore = existingScore + score

            val updatedData = mapOf(
                "username" to username,
                "score" to updatedScore
            )

            database.child(dailyPath).setValue(updatedData).addOnSuccessListener {
                // Success
            }
        }.addOnFailureListener {
            Toast.makeText(this, "Failed to update leaderboard. Please try again.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun isNewDay(savedDate: String, currentDate: String): Boolean {
        return savedDate != currentDate
    }

    private fun saveProgress() {
        val userId = SessionManager.getUserId(this) ?: return
        val db = UserDatabase.getDatabase(this)
        val progressDao = db.dailyExerciseProgressDao()

        val progress = DailyExerciseProgress(
            userId = userId,
            currentGameIndex = currentGameIndex,
            selectedGames = selectedGames.joinToString(","),
            date = currentDate
        )

        CoroutineScope(Dispatchers.IO).launch {
            progressDao.saveProgress(progress)
        }
    }

    private fun loadProgress(onLoaded: (DailyExerciseProgress?) -> Unit) {
        val userId = SessionManager.getUserId(this) ?: return
        val db = UserDatabase.getDatabase(this)
        val progressDao = db.dailyExerciseProgressDao()

        CoroutineScope(Dispatchers.IO).launch {
            val progress = progressDao.getProgress(userId, currentDate)
            withContext(Dispatchers.Main) {
                onLoaded(progress)
            }
        }
    }

    companion object {
        private const val GAME_REQUEST_CODE = 1001
    }
}
