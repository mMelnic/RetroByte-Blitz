package hu.bme.aut.fvf13l.retrobyteblitz.fragments.games

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import hu.bme.aut.fvf13l.retrobyteblitz.databinding.FragmentSequenceGameBinding
import kotlin.math.pow
import kotlin.random.Random

class SequenceGameFragment : Fragment(), CountdownTimerFragment.TimerEndListener {

    private var correctAnswers = 0
    private var totalRounds = 0
    private var currentSequence: List<Int> = listOf()
    private lateinit var binding: FragmentSequenceGameBinding
    private var userInput = ""
    private var correctAnswer: Int = 0
    private val handler = Handler(Looper.getMainLooper())

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSequenceGameBinding.inflate(inflater, container, false)

        val buttons = listOf<Button>(
            binding.button0,
            binding.button1,
            binding.button2,
            binding.button3,
            binding.button4,
            binding.button5,
            binding.button6,
            binding.button7,
            binding.button8,
            binding.button9
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
        currentSequence = generateSequence()
        displaySequence(currentSequence)
        userInput = ""
        totalRounds++
    }

    private fun generateSequence(): List<Int> {
        val sequence = mutableListOf<Int>()

        //val isArithmetic = Random.nextBoolean()
        val isArithmetic = Random.nextInt(100) < 70

        if (isArithmetic) {
            // An arithmetic progression
            val start = Random.nextInt(1, 21)
            val difference = Random.nextInt(1, 16)
            for (i in 0 until 4) {
                sequence.add(start + i * difference)
            }
            correctAnswer = sequence.last() + difference
        } else {
            // A geometric progression
            val start = Random.nextInt(1, 7)
            val ratio = Random.nextInt(2, 5)
            for (i in 0 until 4) {
                sequence.add(start * ratio.toDouble().pow(i.toDouble()).toInt())
            }
            correctAnswer = sequence.last() * ratio
        }

        return sequence
    }

    private fun displaySequence(sequence: List<Int>, userAnswer: String = "?") {
        val sequenceString = sequence.joinToString(", ") + ", " + userAnswer
        binding.sequenceTextView.text = sequenceString
    }

    private fun onNumberSelected(digit: String) {
        userInput += digit
        displaySequence(currentSequence, userInput)
        val correctAnswerString = correctAnswer.toString()
        if (userInput.length == correctAnswerString.length) {
            if (userInput == correctAnswerString) {
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
