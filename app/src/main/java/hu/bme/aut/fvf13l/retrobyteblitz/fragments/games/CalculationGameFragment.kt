package hu.bme.aut.fvf13l.retrobyteblitz.fragments.games

import android.animation.ValueAnimator
import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import hu.bme.aut.fvf13l.retrobyteblitz.databinding.FragmentCalculationGameBinding
import hu.bme.aut.fvf13l.retrobyteblitz.utility.GameDifficultyUtils
import hu.bme.aut.fvf13l.retrobyteblitz.utility.ScoreUtility

class CalculationGameFragment : Fragment(), CountdownTimerFragment.TimerEndListener {

    private lateinit var binding: FragmentCalculationGameBinding
    private var correctAnswersCount = 0
    private var totalRounds = 0
    private var currentDifficulty = 1
    private var userInput = ""
    private val handler = Handler(Looper.getMainLooper())
    private val maxAdditionBound: Int
        get() = when {
            currentDifficulty <= 5 -> 10
            currentDifficulty <= 8 -> 30
            currentDifficulty <= 12 -> 70
            else -> 100
        }

    private val maxMultiplicationBound: Int
        get() = when {
            currentDifficulty <= 4 -> 5
            currentDifficulty <= 7 -> 8
            currentDifficulty <= 11 -> 12
            else -> 15
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCalculationGameBinding.inflate(inflater, container, false)

        val buttons = listOf(
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

        // Set click listeners for all buttons
        buttons.forEach { button ->
            button.setOnClickListener { onNumberSelected(button.text.toString()) }
        }

        // Generate the first equation
        generateNewEquation()

        return binding.root
    }

    private fun generateNewEquation() {
        val equation = generateEquation()
        // Set the equation text with " = ?"
        binding.equationTextView.text = "${equation.first} = ?"
        binding.equationTextView.tag = equation.second
        userInput = "" // Reset user input
        totalRounds++
    }

    private fun generateEquation(): Pair<String, Int> {
        val operators = listOf('+', '-', '*')
        val operation1 = operators.random()

        val firstOperand: Int
        val secondOperand: Int
        var result: Int

        when (operation1) {
            '+' -> {
                firstOperand = (1..maxAdditionBound).random()
                secondOperand = (1..maxAdditionBound).random()
                result = firstOperand + secondOperand
            }
            '-' -> {
                firstOperand = (1..maxAdditionBound).random()
                secondOperand = (1..firstOperand).random()
                result = firstOperand - secondOperand
            }
            '*' -> {
                firstOperand = (1..maxMultiplicationBound).random()
                secondOperand = (1..maxMultiplicationBound).random()
                result = firstOperand * secondOperand
            }
            else -> throw IllegalStateException("Unexpected operator: $operation1")
        }

        if (currentDifficulty > 5) {
            val operation2 = operators.random()
            val thirdOperand: Int

            when (operation2) {
                '+' -> {
                    thirdOperand = (1..maxAdditionBound).random()
                    result += thirdOperand
                }
                '-' -> {
                    thirdOperand = (1..result).random()
                    result -= thirdOperand
                }
                '*' -> {
                    thirdOperand = when (operation1) {
                        '-' -> {
                            // Ensure firstOperand >= secondOperand * thirdOperand
                            val maxThirdOperand = (firstOperand / secondOperand).coerceAtLeast(1)
                            (1..maxThirdOperand).random()
                        }
                        else -> (1..maxMultiplicationBound).random()
                    }
                    result *= thirdOperand
                }
                else -> throw IllegalStateException("Unexpected operator: $operation2")
            }

            val combinedEquation = "$firstOperand $operation1 $secondOperand $operation2 $thirdOperand"
            return Pair(combinedEquation, result)
        }

        val equation = "$firstOperand $operation1 $secondOperand"
        return Pair(equation, result)
    }

    private fun onNumberSelected(digit: String) {
        userInput += digit
        displayEquationWithInput()
        val correctAnswer = binding.equationTextView.tag as? Int
        val correctAnswerString = correctAnswer?.toString()

        if (correctAnswerString != null && userInput.length == correctAnswerString.length) {
            if (userInput == correctAnswerString) {
                correctAnswersCount++
                currentDifficulty++
            }
            updateScore()
            handler.postDelayed({
                generateNewEquation()
            }, 500)
        }
    }

    private fun displayEquationWithInput() {
        val equationWithoutAnswer = binding.equationTextView.text.toString().substringBefore("=")
        binding.equationTextView.text = "$equationWithoutAnswer= $userInput"
    }

    private fun updateScore() {
        binding.scoreTextView.text = "Correct: $correctAnswersCount / $totalRounds"
    }

    private fun displayFinalScore() {
        var gameName = requireActivity().intent.getStringExtra("GAME_NAME") ?: "Default Game"
        val score = GameDifficultyUtils.calculateScore(gameName, correctAnswersCount)

        val scoreLayout = ScoreUtility.createScoreLayout(requireContext(), score) { _ ->
        }

        AlertDialog.Builder(requireContext())
            .setTitle("Game Over")
            .setView(scoreLayout)
            .setMessage("You solved $correctAnswersCount equations correctly!")
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