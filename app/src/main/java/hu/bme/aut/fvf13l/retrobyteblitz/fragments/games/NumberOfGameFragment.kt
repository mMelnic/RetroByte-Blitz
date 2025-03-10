package hu.bme.aut.fvf13l.retrobyteblitz.fragments.games

import android.app.Activity
import android.content.Intent
import hu.bme.aut.fvf13l.retrobyteblitz.R
import hu.bme.aut.fvf13l.retrobyteblitz.databinding.FragmentNumberOfGameBinding

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import hu.bme.aut.fvf13l.retrobyteblitz.utility.GameDifficultyUtils
import hu.bme.aut.fvf13l.retrobyteblitz.utility.ScoreUtility

class NumberOfGameFragment : Fragment(), CountdownTimerFragment.TimerEndListener {

    private lateinit var binding: FragmentNumberOfGameBinding
    private var userAnswer: String = ""
    private var correctAnswer: Int = 0
    private var correctAnswers = 0
    private var totalRounds = 0
    private val characters = listOf(R.drawable.pilot, R.drawable.doctor, R.drawable.engineer)
    private val handler = Handler(Looper.getMainLooper())

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentNumberOfGameBinding.inflate(inflater, container, false)

        val buttons = listOf(
            binding.button0, binding.button1, binding.button2, binding.button3, binding.button4,
            binding.button5, binding.button6, binding.button7, binding.button8, binding.button9
        )

        buttons.forEach { button ->
            button.setOnClickListener {
                onNumberSelected(button.text.toString())
            }
        }

        startNewRound()
        return binding.root
    }

    private fun startNewRound() {
        // Randomly distributing characters in the grid
        val gridItems = MutableList(16) { characters.random() }

        val imageViews = listOf(
            binding.imageView, binding.imageView2, binding.imageView3, binding.imageView4,
            binding.imageView5, binding.imageView6, binding.imageView7, binding.imageView8,
            binding.imageView9, binding.imageView10, binding.imageView11, binding.imageView12,
            binding.imageView13, binding.imageView14, binding.imageView15, binding.imageView16
        )

        imageViews.forEachIndexed { index, imageView ->
            imageView.setImageResource(gridItems[index])
        }

        val targetCharacter = characters.random()
        correctAnswer = gridItems.count { it == targetCharacter }
        binding.questionImageView.setImageResource(targetCharacter)
        binding.questionMarkTextView.text = "?"
        userAnswer = ""
        totalRounds++
    }

    private fun onNumberSelected(digit: String) {
        userAnswer += digit
        binding.questionMarkTextView.text = userAnswer

        val correctAnswerString = correctAnswer.toString()
        if (userAnswer.length == correctAnswerString.length) {
            if (userAnswer == correctAnswerString) {
                correctAnswers++
            }
            updateScore()
            handler.postDelayed({
                startNewRound()
            }, 500)
        }
    }

    private fun updateScore() {
        binding.scoreTextView.text = "Correct: $correctAnswers / $totalRounds"
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
            .setOnDismissListener { sendResultAndFinish(score) }
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

