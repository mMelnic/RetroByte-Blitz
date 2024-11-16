package hu.bme.aut.fvf13l.retrobyteblitz.fragments.games

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import hu.bme.aut.fvf13l.retrobyteblitz.R

class CalculationGameFragment : Fragment(), CountdownTimerFragment.TimerEndListener {

    private lateinit var equationTextView: TextView
    private lateinit var answerEditText: EditText
    private lateinit var submitButton: Button
    private var correctAnswersCount = 0
    private var currentDifficulty = 1
    private val maxAdditionBound = when {
        currentDifficulty <= 5 -> 10 // Levels 1-5: Prefer numbers <= 10
        currentDifficulty <= 8 -> 30 // Levels 6-8: Increase to <= 30
        currentDifficulty <= 12 -> 70 // Levels 9-12: Increase to <= 70
        else -> 100 // Level 13 and beyond: Up to <= 100
    }

    private val maxMultiplicationBound = when {
        currentDifficulty <= 4 -> 5 // Levels 1-4: Prefer numbers <= 5
        currentDifficulty <= 7 -> 8 // Levels 5-7: Increase to <= 8
        currentDifficulty <= 11 -> 12 // Levels 8-11: Increase to <= 12
        else -> 15 // Level 12 and beyond: Up to <= 15
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_calculation_game, container, false)
        equationTextView = rootView.findViewById(R.id.equationTextView)
        answerEditText = rootView.findViewById(R.id.answerEditText)
        submitButton = rootView.findViewById(R.id.submitButton)

        submitButton.setOnClickListener {
            checkAnswer()
        }

        generateNewEquation()
        return rootView
    }

    private fun generateNewEquation() {
        val equation = generateEquation()
        equationTextView.text = equation.first
        equationTextView.tag = equation.second
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
                    if (operation1 == '-') {
                        thirdOperand = (1..(result / 2).coerceAtLeast(1)).random()
                    } else {
                        thirdOperand = (1..maxMultiplicationBound).random()
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

    private fun checkAnswer() {
        val userAnswer = answerEditText.text.toString().toIntOrNull()
        val correctAnswer = equationTextView.tag as? Int

        if (userAnswer != null && correctAnswer != null && userAnswer == correctAnswer) {
            correctAnswersCount++
            currentDifficulty++
            answerEditText.text.clear()
            generateNewEquation()
        } else {
            answerEditText.text.clear()
            generateNewEquation()
        }
    }

    fun displayFinalScore() {
        AlertDialog.Builder(requireContext())
            .setTitle("Game Over")
            .setMessage("You solved $correctAnswersCount equations correctly!")
            .setPositiveButton("OK") { _, _ -> activity?.finish() }
            .setOnDismissListener { activity?.finish() }
            .show()
    }

    override fun onTimerEnd() {
        displayFinalScore()
    }
}
