package hu.bme.aut.fvf13l.retrobyteblitz.fragments.games

import android.app.Activity
import android.content.Intent
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
import androidx.core.content.ContextCompat
import hu.bme.aut.fvf13l.retrobyteblitz.R
import hu.bme.aut.fvf13l.retrobyteblitz.utility.GameDifficultyUtils
import hu.bme.aut.fvf13l.retrobyteblitz.utility.ScoreUtility

class GridMemoryGameFragment : Fragment(), CountdownTimerFragment.TimerEndListener {

    private lateinit var binding: FragmentGridMemoryGameBinding

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
        binding = FragmentGridMemoryGameBinding.inflate(inflater, container, false)
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
                binding.gridLayout.viewTreeObserver.removeOnGlobalLayoutListener(this)

                val gridWidth = binding.gridLayout.width - binding.gridLayout.paddingLeft - binding.gridLayout.paddingRight
                val gridHeight = binding.gridLayout.height - binding.gridLayout.paddingTop - binding.gridLayout.paddingBottom

                val numColumns = binding.gridLayout.columnCount
                val numRows = binding.gridLayout.rowCount

                val cellWidth = (gridWidth - (numColumns - 1) * 22) / numColumns  // Adjust for margins
                val cellHeight = (gridHeight - (numRows - 1) * 22) / numRows  // Adjust for margins

                println("Calculated cell size: $cellWidth x $cellHeight")

                for (i in 0 until gridSize) {
                    val view = View(context)
                    view.setBackgroundColor(Color.WHITE)

                    val params = GridLayout.LayoutParams().apply {
                        width = cellWidth
                        height = cellHeight
                        setMargins(12, 12, 12, 12)
                    }

                    view.layoutParams = params
                    view.setOnClickListener { onSquareClicked(i) }
                    binding.gridLayout.addView(view)

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
            val custom = ContextCompat.getColor(requireContext(), R.color.yellow_pastel)
            view.setBackgroundColor(custom)
        }

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

        handler.postDelayed({
            correctSquares.forEach {
                val view = binding.gridLayout.getChildAt(it)
                val custom = ContextCompat.getColor(requireContext(), R.color.yellow_pastel)
                view.setBackgroundColor(custom)
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

    private fun displayFinalScore() {
        val gameName = requireActivity().intent.getStringExtra("GAME_NAME") ?: "Default Game"
        val score = GameDifficultyUtils.calculateScore(gameName, successfulRounds)

        val scoreLayout = ScoreUtility.createScoreLayout(requireContext(), score) { _ ->
        }

        AlertDialog.Builder(requireContext())
            .setTitle("Game Over")
            .setView(scoreLayout)
            .setMessage("You solved $successfulRounds out of $totalRounds rounds!")
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
}


