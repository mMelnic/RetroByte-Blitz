package hu.bme.aut.fvf13l.retrobyteblitz.fragments.games

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import hu.bme.aut.fvf13l.retrobyteblitz.R
import hu.bme.aut.fvf13l.retrobyteblitz.databinding.FragmentStroopGameBinding
import hu.bme.aut.fvf13l.retrobyteblitz.utility.GameDifficultyUtils
import hu.bme.aut.fvf13l.retrobyteblitz.utility.ScoreUtility

class StroopGameFragment : Fragment(), CountdownTimerFragment.TimerEndListener {

    private lateinit var binding: FragmentStroopGameBinding
    private var correctAnswers = 0
    private var totalRounds = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentStroopGameBinding.inflate(inflater, container, false)
        setupGame()
        return binding.root
    }

    private fun setupGame() {
        loadNewRound()
        setupButtonListeners()
    }

    private fun loadNewRound() {
        totalRounds++

        val colorOptions = listOf(
            Pair("Red", R.color.red_color),
            Pair("Green", R.color.green_color),
            Pair("Blue", R.color.blue_color),
            Pair("Yellow", R.color.yellow_color),
            Pair("Purple", R.color.purple_500),
            Pair("Orange", R.color.orange_color)
        )

        // Randomly select text and color independently
        val (textColorName, _) = colorOptions.random()
        val (_, displayColorRes) = colorOptions.random()

        // Set the `tvColor` text and color
        binding.tvColor.text = textColorName
        binding.tvColor.setTextColor(requireContext().getColor(displayColorRes))
    }


    private fun setupButtonListeners() {
        binding.btnRed.setOnClickListener {
            checkAnswer(R.color.red_color)
        }
        binding.btnGreen.setOnClickListener {
            checkAnswer(R.color.green_color)
        }
        binding.btnBlue.setOnClickListener {
            checkAnswer(R.color.blue_color)
        }
        binding.btnYellow.setOnClickListener {
            checkAnswer(R.color.yellow_color)
        }
        binding.btnOrange.setOnClickListener {
            checkAnswer(R.color.orange_color)
        }
        binding.btnPurple.setOnClickListener {
            checkAnswer(R.color.purple_500)
        }
    }

    private fun checkAnswer(selectedColorResId: Int) {
        // Get the actual displayed color of `tvColor`
        val displayedColorId = (binding.tvColor.currentTextColor)

        // Check if the button color matches the text color of `tvColor`
        if (displayedColorId == requireContext().getColor(selectedColorResId)) {
            correctAnswers++
        }
        updateScore()
        loadNewRound()
    }


    private fun updateScore() {
        binding.tvScore.text = "Score: $correctAnswers/$totalRounds"
    }

    private fun displayFinalScore() {
        val gameName = requireActivity().intent.getStringExtra("GAME_NAME") ?: "Default Game"
        val score = GameDifficultyUtils.calculateScore(gameName, correctAnswers)

        val scoreLayout = ScoreUtility.createScoreLayout(requireContext(), score) { _ ->
        }

        AlertDialog.Builder(requireContext())
            .setTitle("Game Over")
            .setView(scoreLayout)
            .setMessage("You solved $correctAnswers out of $totalRounds rounds!")
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
