package hu.bme.aut.fvf13l.retrobyteblitz.fragments.games

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import hu.bme.aut.fvf13l.retrobyteblitz.databinding.FragmentMovingSumGameBinding
import hu.bme.aut.fvf13l.retrobyteblitz.utility.GameDifficultyUtils
import hu.bme.aut.fvf13l.retrobyteblitz.utility.ScoreUtility
import kotlin.random.Random

class MovingSumGameFragment : Fragment(), CountdownTimerFragment.TimerEndListener {

    private lateinit var binding: FragmentMovingSumGameBinding
    private var userInput = ""
    private var correctAnswer = 0
    private var correctAnswers = 0
    private var totalRounds = 0
    private val handler = Handler(Looper.getMainLooper())

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMovingSumGameBinding.inflate(inflater, container, false)

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
        binding.numberContainer.removeAllViews()
        binding.questionTextView.text = "Sum of moving ?"

        val baseNumbers = 5 // Minimum numbers to display in the first round
        val maxNumbers = 20 // Maximum numbers allowed on the board
        val numbersToDisplay = (baseNumbers + totalRounds).coerceAtMost(maxNumbers)

        val randomNumbers = List(numbersToDisplay) { Random.nextInt(1, 10) }
        correctAnswer = 0

        // List to track positions of non-moving numbers to prevent overlap
        val existingPositions = mutableListOf<Pair<Float, Float>>()

        randomNumbers.forEachIndexed { index, number ->
            val textView = TextView(requireContext()).apply {
                text = if (number == 6 || number == 9) {
                    "$number."
                } else {
                    number.toString()
                }
                textSize = 27f
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
            }
            binding.numberContainer.addView(textView)

            if (index % 2 == 0) {
                // Moving numbers
                correctAnswer += number
                createRandomMovement(textView)
            } else {
                // Non-moving numbers
                binding.numberContainer.post {
                    val containerWidth = binding.numberContainer.width
                    val containerHeight = binding.numberContainer.height

                    textView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)
                    val viewWidth = textView.measuredWidth
                    val viewHeight = textView.measuredHeight

                    val (x, y) = generateNonOverlappingPosition(
                        existingPositions, viewWidth, viewHeight, containerWidth, containerHeight
                    )
                    textView.x = x
                    textView.y = y
                    existingPositions.add(Pair(x, y))
                }
            }
        }

        userInput = ""
        totalRounds++
    }

    private fun createRandomMovement(view: View) {
        // Animation delay until the container size is available
        binding.numberContainer.post {
            val containerWidth = binding.numberContainer.width
            val containerHeight = binding.numberContainer.height
            val randomX = Random.nextInt(0, containerWidth - view.width).toFloat()
            val randomY = Random.nextInt(0, containerHeight - view.height).toFloat()

            val animX = ObjectAnimator.ofFloat(view, "x", view.x, randomX).apply {
                duration = (1000..3000).random().toLong()
                repeatCount = ObjectAnimator.INFINITE
                repeatMode = ObjectAnimator.REVERSE
            }

            val animY = ObjectAnimator.ofFloat(view, "y", view.y, randomY).apply {
                duration = (1000..3000).random().toLong()
                repeatCount = ObjectAnimator.INFINITE
                repeatMode = ObjectAnimator.REVERSE
            }

            val rotationAnim = ObjectAnimator.ofFloat(view, "rotation", 0f, 360f).apply {
                duration = (2000..4000).random().toLong()
                repeatCount = ObjectAnimator.INFINITE
                repeatMode = ObjectAnimator.RESTART
            }

            AnimatorSet().apply {
                playTogether(animX, animY, rotationAnim)
                start()
            }
        }
    }

    private fun generateNonOverlappingPosition(
        existingPositions: MutableList<Pair<Float, Float>>,
        viewWidth: Int,
        viewHeight: Int,
        containerWidth: Int,
        containerHeight: Int
    ): Pair<Float, Float> {
        var x: Float
        var y: Float
        var isOverlapping: Boolean

        do {
            x = Random.nextInt(0, containerWidth - viewWidth).toFloat()
            y = Random.nextInt(0, containerHeight - viewHeight).toFloat()

            isOverlapping = existingPositions.any { (existingX, existingY) ->
                val overlapX = (x < existingX + viewWidth) && (x + viewWidth > existingX)
                val overlapY = (y < existingY + viewHeight) && (y + viewHeight > existingY)
                overlapX && overlapY
            }
        } while (isOverlapping)
        existingPositions.add(Pair(x, y))

        return Pair(x, y)
    }



    private fun onNumberSelected(digit: String) {
        userInput += digit
        binding.questionTextView.text = "Sum of moving $userInput"

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

    private fun displayFinalScore() {
        val gameName = requireActivity().intent.getStringExtra("GAME_NAME") ?: "Default Game"
        val score = GameDifficultyUtils.calculateScore(gameName, correctAnswers)

        val scoreLayout = ScoreUtility.createScoreLayout(requireContext(), score) { _ ->
        }

        AlertDialog.Builder(requireContext())
            .setTitle("Game Over")
            .setView(scoreLayout)
            .setMessage("You solved $correctAnswers rounds!")
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
