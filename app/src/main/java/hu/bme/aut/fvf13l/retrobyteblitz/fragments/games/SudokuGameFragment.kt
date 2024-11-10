package hu.bme.aut.fvf13l.retrobyteblitz.fragments.games

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
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.lifecycleScope
import hu.bme.aut.fvf13l.retrobyteblitz.MainActivity
import hu.bme.aut.fvf13l.retrobyteblitz.data.api.RetrofitClient
import hu.bme.aut.fvf13l.retrobyteblitz.databinding.FragmentSudokuGameBinding
import kotlinx.coroutines.launch


class SudokuGameFragment : Fragment() {

    private var _binding: FragmentSudokuGameBinding? = null
    private val binding get() = _binding!!
    private lateinit var solutionGrid: List<List<Int>>
    private lateinit var currentGrid: Array<Array<EditText>>
    private val difficultyTimeLimits = mapOf(
        "easy" to 300000L,    // 5 minutes for easy
        "medium" to 450000L,  // 7.5 minutes for medium
        "hard" to 600000L     // 10 minutes for hard
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Register the callback in the fragment but delegate the handling to the activity
        requireActivity().onBackPressedDispatcher.addCallback(this) {
            (activity as? MainActivity)?.handleBackPressed()
        }
    }

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
                // 1. Log the raw response immediately after receiving it
                val response = RetrofitClient.api.getSudokuGrid(query)  // Retrofit will directly return SudokuResponse

                // 2. Log the entire response body (no need for body() if Retrofit already returns the parsed object)
                Log.d("SudokuGameFragment", "Full API response: $response")

                // 3. Log the response to verify its structure
                Log.d("SudokuGameFragment", "Parsed response: $response")

                // 4. Check if 'newboard' exists and is not null, then process the response
                val newboard = response.newboard  // Directly access the newboard from the parsed response
                if (newboard != null) {
                    // 5. Log the 'newboard' data to verify its structure
                    Log.d("SudokuGameFragment", "newboard: $newboard")

                    // 6. Check if the 'grids' list is not empty
                    if (newboard.grids?.isNotEmpty() == true) {
                        // 7. Access the first grid in the 'grids' list
                        val grid = newboard.grids[0]

                        // 8. Log the fetched grid details
                        Log.d("SudokuGameFragment", "Fetched grid details - value: ${grid.value}, solution: ${grid.solution}, difficulty: ${grid.difficulty}")

                        // 9. Display the Sudoku grid and set up the timer
                        displaySudokuGrid(grid.value, grid.solution)
                        val difficulty = grid.difficulty.lowercase() // "easy", "medium", "hard"
                        val timeLimit = difficultyTimeLimits[difficulty] ?: 300000L
                        (activity as MainActivity).startTimer(timeLimit)
                        addTextWatchers()

                    } else {
                        // Handle the case where the 'grids' field is empty
                        Log.e("SudokuGameFragment", "Grids list is empty")
                        Toast.makeText(context, "Failed to fetch Sudoku grid", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    // Handle the case where 'newboard' is null
                    Log.e("SudokuGameFragment", "newboard field is null or empty")
                    Toast.makeText(context, "Failed to fetch Sudoku grid", Toast.LENGTH_SHORT).show()
                }

            } catch (e: Exception) {
                // Log the error if an exception is thrown
                Log.e("SudokuGameFragment", "Error fetching Sudoku grid", e)
                Toast.makeText(context, "Error fetching Sudoku grid", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun displaySudokuGrid(initialGrid: List<List<Int>>, solutionGrid: List<List<Int>>) {
        this.solutionGrid = solutionGrid
        currentGrid = Array(9) { arrayOfNulls<EditText>(9) as Array<EditText> }
        binding.sudokuGrid.removeAllViews()

        // Loop to create the 9x9 grid
        for (row in 0 until 9) {
            for (col in 0 until 9) {
                val cell = EditText(requireContext()).apply {
                    setTextSize(16f)
                    textAlignment = View.TEXT_ALIGNMENT_CENTER
                    inputType = InputType.TYPE_CLASS_NUMBER
                    gravity = Gravity.CENTER
                    // Disable input for filled cells
                    if (initialGrid[row][col] != 0) {
                        setText(initialGrid[row][col].toString())
                        isEnabled = false
                    }
                }

                // Create grid layout parameters to define position and layout behavior
                val params = GridLayout.LayoutParams().apply {
                    rowSpec = GridLayout.spec(row)
                    columnSpec = GridLayout.spec(col)

                    // Set both width and height to 0 and use weight to distribute space
                    width = 0
                    height = 0
                    // Set weight for each cell to make them stretch evenly
                    rowSpec = GridLayout.spec(row, 1f) // 1f weight for row
                    columnSpec = GridLayout.spec(col, 1f) // 1f weight for column
                    setGravity(Gravity.FILL)
                }

                // Add the EditText cell to the grid
                binding.sudokuGrid.addView(cell, params)

                // Store the reference of the EditText cell
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

