package hu.bme.aut.fvf13l.retrobyteblitz.fragments.games

import hu.bme.aut.fvf13l.retrobyteblitz.R
import hu.bme.aut.fvf13l.retrobyteblitz.databinding.FragmentNumberOfGameBinding

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import androidx.core.view.setPadding
import androidx.fragment.app.Fragment
import androidx.gridlayout.widget.GridLayout

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
        // Randomly distribute characters in the grid
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

        // Choose a character type for the user to count
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

        // Check if the user input matches the length of the correct answer
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

    override fun onTimerEnd() {
        AlertDialog.Builder(requireContext())
            .setTitle("Game Over")
            .setMessage("Score: $correctAnswers/$totalRounds")
            .setPositiveButton("OK") { _, _ -> activity?.finish() }
            .setOnDismissListener { activity?.finish() }
            .show()
    }
}

