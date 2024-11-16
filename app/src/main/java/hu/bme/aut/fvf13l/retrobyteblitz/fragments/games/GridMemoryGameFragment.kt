package hu.bme.aut.fvf13l.retrobyteblitz.fragments.games

import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.gridlayout.widget.GridLayout
import hu.bme.aut.fvf13l.retrobyteblitz.databinding.FragmentGridMemoryGameBinding
import kotlin.random.Random
import android.view.ViewTreeObserver
import androidx.appcompat.app.AlertDialog

class GridMemoryGameFragment : Fragment(), CountdownTimerFragment.TimerEndListener {

    private var _binding: FragmentGridMemoryGameBinding? = null
    private val binding get() = _binding!!

    private var gridSize = 9
    private var round = 1
    private var successfulRounds = 0
    private var totalRounds = 0
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var correctSquares: List<Int>
    private lateinit var userSelectedSquares: MutableList<Int>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGridMemoryGameBinding.inflate(inflater, container, false)
        setupGrid()
        startNewRound()
        return binding.root
    }

    private fun setupGrid() {
        binding.gridLayout.removeAllViews()
        when (gridSize) {
            9 -> {
                binding.gridLayout.columnCount = 3
                binding.gridLayout.rowCount = 3
            }
            12 -> {
                binding.gridLayout.columnCount = 4
                binding.gridLayout.rowCount = 3
            }
            16 -> {
                binding.gridLayout.columnCount = 4
                binding.gridLayout.rowCount = 4
            }
        }

        binding.gridLayout.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                // Remove listener to prevent multiple calls
                binding.gridLayout.viewTreeObserver.removeOnGlobalLayoutListener(this)

                // Calculate cell dimensions
                val gridWidth = binding.gridLayout.width - binding.gridLayout.paddingLeft - binding.gridLayout.paddingRight
                val gridHeight = binding.gridLayout.height - binding.gridLayout.paddingTop - binding.gridLayout.paddingBottom

                val numColumns = binding.gridLayout.columnCount
                val numRows = binding.gridLayout.rowCount

                val cellWidth = (gridWidth - (numColumns - 1) * 16) / numColumns  // Adjust for margins
                val cellHeight = (gridHeight - (numRows - 1) * 16) / numRows  // Adjust for margins

                println("Calculated cell size: $cellWidth x $cellHeight")

                for (i in 0 until gridSize) {
                    val view = View(context)
                    view.setBackgroundColor(Color.WHITE)

                    val params = GridLayout.LayoutParams().apply {
                        width = cellWidth
                        height = cellHeight
                        setMargins(8, 8, 8, 8)
                    }

                    view.layoutParams = params
                    view.setOnClickListener { onSquareClicked(i) }
                    binding.gridLayout.addView(view)

                    // Debug log
                    println("View added to GridLayout: $i with size $cellWidth x $cellHeight")
                }
            }
        })
    }


    private fun onSquareClicked(index: Int) {
        val view = binding.gridLayout.getChildAt(index)
        if (userSelectedSquares.contains(index)) {
            userSelectedSquares.remove(index)
            view.setBackgroundColor(Color.WHITE)
        } else {
            userSelectedSquares.add(index)
            view.setBackgroundColor(Color.YELLOW)
        }
        println("Selected squares for this round: $userSelectedSquares")

        if (userSelectedSquares.size == correctSquares.size) {
            checkUserSolution()
        }
    }


    private fun startNewRound() {
        userSelectedSquares = mutableListOf()

        val targetSize = (round + 2).coerceAtMost(gridSize / 2)
        val uniqueSquares = mutableSetOf<Int>()

        while (uniqueSquares.size < targetSize) {
            uniqueSquares.add(Random.nextInt(gridSize))
        }
        correctSquares = uniqueSquares.toList()

        binding.roundInfo.text = "Round $round"
        binding.scoreInfo.text = "Score: $successfulRounds/$totalRounds"

        println("Correct squares for this round: $correctSquares")

        handler.postDelayed({
            correctSquares.forEach {
                val view = binding.gridLayout.getChildAt(it)
                view.setBackgroundColor(Color.YELLOW)
            }

            handler.postDelayed({
                correctSquares.forEach {
                    val view = binding.gridLayout.getChildAt(it)
                    view.setBackgroundColor(Color.WHITE)
                }
            }, 1000)

        }, 1000)

        totalRounds++
    }


    private fun checkUserSolution() {
        if (userSelectedSquares.sorted() == correctSquares.sorted()) {
            successfulRounds++
        }
        round++

        if (round == 4) gridSize = 12
        if (round == 7) gridSize = 16

        handler.postDelayed({
            setupGrid()
            startNewRound()
        }, 500)
    }

    override fun onTimerEnd() {
        AlertDialog.Builder(requireContext())
            .setTitle("Game Over")
            .setMessage("Score: $successfulRounds/$totalRounds")
            .setPositiveButton("OK") { _, _ -> activity?.finish() }
            .setOnDismissListener { activity?.finish() }
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

