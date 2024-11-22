package hu.bme.aut.fvf13l.retrobyteblitz.fragments.games

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.GridLayout
import android.widget.Toast
import androidx.activity.addCallback
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.lifecycleScope
import hu.bme.aut.fvf13l.retrobyteblitz.MainActivity
import hu.bme.aut.fvf13l.retrobyteblitz.data.api.RetrofitClient
import hu.bme.aut.fvf13l.retrobyteblitz.databinding.FragmentSudokuGameBinding
import hu.bme.aut.fvf13l.retrobyteblitz.utility.GameDifficultyUtils
import hu.bme.aut.fvf13l.retrobyteblitz.utility.ScoreUtility
import kotlinx.coroutines.launch


class SudokuGameFragment : Fragment(), CountdownTimerFragment.TimerEndListener {

    private var _binding: FragmentSudokuGameBinding? = null
    private val binding get() = _binding!!
    private lateinit var solutionGrid: List<List<Int>>
    private lateinit var currentGrid: Array<Array<EditText>>
    private var successfulRounds = 0
//    private val difficultyTimeLimits = mapOf(
//        "easy" to 300000L,    // 5 minutes for easy
//        "medium" to 450000L,  // 7.5 minutes for medium
//        "hard" to 600000L     // 10 minutes for hard
//    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSudokuGameBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fetchNewSudokuGrid()
    }

    private fun fetchNewSudokuGrid() {
        val query = "{ newboard(limit: 1) { grids { value solution difficulty } } }"
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.api.getSudokuGrid(query)

                // 1. Log the entire response body
                Log.d("SudokuGameFragment", "Full API response: $response")

                // 2. Log the response to verify its structure
                Log.d("SudokuGameFragment", "Parsed response: $response")

                val newboard = response.newboard
                if (newboard != null) {
                    // 4. Log the 'newboard' data to verify its structure
                    Log.d("SudokuGameFragment", "newboard: $newboard")

                    if (newboard.grids?.isNotEmpty() == true) {
                        val grid = newboard.grids[0]

                        // 5. Log the fetched first grid details
                        Log.d("SudokuGameFragment", "Fetched grid details - value: ${grid.value}, solution: ${grid.solution}, difficulty: ${grid.difficulty}")

                        displaySudokuGrid(grid.value, grid.solution)
                        val difficulty = grid.difficulty.lowercase() // "easy", "medium", "hard"
                        //val timeLimit = difficultyTimeLimits[difficulty] ?: 300000L
                        addTextWatchers()

                    } else {
                        Log.e("SudokuGameFragment", "Grids list is empty")
                        Toast.makeText(context, "Failed to fetch Sudoku grid", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Log.e("SudokuGameFragment", "newboard field is null or empty")
                    Toast.makeText(context, "Failed to fetch Sudoku grid", Toast.LENGTH_SHORT).show()
                }

            } catch (e: Exception) {
                Log.e("SudokuGameFragment", "Error fetching Sudoku grid", e)
                Toast.makeText(context, "Error fetching Sudoku grid", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun displaySudokuGrid(initialGrid: List<List<Int>>, solutionGrid: List<List<Int>>) {
        this.solutionGrid = solutionGrid
        currentGrid = Array(9) { arrayOfNulls<EditText>(9) as Array<EditText> }
        binding.sudokuGrid.removeAllViews()

        for (row in 0 until 9) {
            for (col in 0 until 9) {
                val cell = EditText(requireContext()).apply {
                    setTextSize(16f)
                    textAlignment = View.TEXT_ALIGNMENT_CENTER
                    inputType = InputType.TYPE_CLASS_NUMBER
                    gravity = Gravity.CENTER
                    // Disabled input for filled cells
                    if (initialGrid[row][col] != 0) {
                        setText(initialGrid[row][col].toString())
                        isEnabled = false
                    }
                }

                val params = GridLayout.LayoutParams().apply {
                    rowSpec = GridLayout.spec(row)
                    columnSpec = GridLayout.spec(col)

                    width = 0
                    height = 0
                    rowSpec = GridLayout.spec(row, 1f)
                    columnSpec = GridLayout.spec(col, 1f)
                    setGravity(Gravity.FILL)
                }

                binding.sudokuGrid.addView(cell, params)
                currentGrid[row][col] = cell
            }
        }
    }


    private fun addTextWatchers() {
        for (row in 0 until 9) {
            for (col in 0 until 9) {
                if (currentGrid[row][col].isEnabled) {
                    currentGrid[row][col].addTextChangedListener(object : TextWatcher {
                        override fun afterTextChanged(s: Editable?) {
                            onGridSolved { fetchNewSudokuGrid() }
                        }
                        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                    })
                }
            }
        }
    }

    private fun onGridSolved(callback: () -> Unit) {
        if (checkIfGridSolved()) {
            successfulRounds++
            callback()
        }
    }

    private fun checkIfGridSolved(): Boolean {
        for (row in 0 until 9) {
            for (col in 0 until 9) {
                val userEntry = currentGrid[row][col].text.toString().toIntOrNull() ?: 0
                if (userEntry != solutionGrid[row][col]) {
                    return false
                }
            }
        }
        return true
    }

    private fun displayFinalScore() {
        val gameName = requireActivity().intent.getStringExtra("GAME_NAME") ?: "Default Game"
        val score = GameDifficultyUtils.calculateScore(gameName, successfulRounds)

        val scoreLayout = ScoreUtility.createScoreLayout(requireContext(), score) { _ ->
        }

        AlertDialog.Builder(requireContext())
            .setTitle("Game Over")
            .setView(scoreLayout)
            .setMessage("You solved $successfulRounds rounds!")
            .setPositiveButton("OK") { _, _ -> sendResultAndFinish(score) }
            .setOnDismissListener { activity?.finish() }
            .show()

    }

    private fun sendResultAndFinish(score: Int) {
        val resultIntent = Intent().apply {
            putExtra("SCORE", score)
        }
        activity?.setResult(Activity.RESULT_OK, resultIntent)
        activity?.finish()
    }

    override fun onTimerEnd() {
        displayFinalScore()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

