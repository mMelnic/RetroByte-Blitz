package hu.bme.aut.fvf13l.retrobyteblitz.fragments.games

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import hu.bme.aut.fvf13l.retrobyteblitz.R
import hu.bme.aut.fvf13l.retrobyteblitz.databinding.FragmentRomanNumeralsGameBinding
import hu.bme.aut.fvf13l.retrobyteblitz.utility.GameDifficultyUtils
import hu.bme.aut.fvf13l.retrobyteblitz.utility.ScoreUtility
import kotlin.random.Random

class RomanNumeralsGameFragment : Fragment(), CountdownTimerFragment.TimerEndListener {

    private lateinit var binding: FragmentRomanNumeralsGameBinding

    private val handler = Handler(Looper.getMainLooper())
    private val movingViews = mutableListOf<TextView>()
    private var selectedTextView: TextView? = null

    private var matchedPairs = 0
    private var round = 1
    private var successfulRounds = 0
    private val romanArabicPairs = mutableMapOf<TextView, TextView>()

    private fun generateRomanNumeral(level: Int, usedValues: MutableSet<Int>): Pair<String, Int> {
        val maxValue = when {
            level <= 3 -> 20
            level <= 7 -> 50
            else -> 100
        }

        var value: Int
        do {
            value = Random.nextInt(1, maxValue + 1) // To avoid duplicate pairs
        } while (usedValues.contains(value))
        usedValues.add(value)

        val roman = arabicToRoman(value)
        return Pair(roman, value)
    }

    private fun arabicToRoman(number: Int): String {
        val romanNumerals = listOf(
            Pair(1000, "M"), Pair(900, "CM"), Pair(500, "D"),
            Pair(400, "CD"), Pair(100, "C"), Pair(90, "XC"),
            Pair(50, "L"), Pair(40, "XL"), Pair(10, "X"),
            Pair(9, "IX"), Pair(5, "V"), Pair(4, "IV"), Pair(1, "I")
        )
        var num = number
        val builder = StringBuilder()

        for ((value, numeral) in romanNumerals) {
            while (num >= value) {
                builder.append(numeral)
                num -= value
            }
        }
        return builder.toString()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentRomanNumeralsGameBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        startNewRound()
    }

    private fun startNewRound() {
        binding.gameContainer.removeAllViews()
        movingViews.clear()
        romanArabicPairs.clear()
        binding.scoreTextView.text = "Round: $round"
        matchedPairs = 0

        val pairsCount = 1 + round
        val usedValues = mutableSetOf<Int>()

        for (i in 1..pairsCount) {
            val (roman, value) = generateRomanNumeral(round, usedValues)

            val romanTextView = createTextView(roman)
            val arabicTextView = createTextView(value.toString())

            romanArabicPairs[romanTextView] = arabicTextView
            romanArabicPairs[arabicTextView] = romanTextView

            movingViews.add(romanTextView)
            movingViews.add(arabicTextView)

            binding.gameContainer.addView(romanTextView)
            binding.gameContainer.addView(arabicTextView)
        }

        startMovingViews()
    }

    private fun createTextView(text: String): TextView {
        return TextView(requireContext()).apply {
            this.text = text
            textSize = 24f
            setTextColor(Color.BLACK)
            setTypeface(Typeface.create("sans-serif", Typeface.BOLD))

            setBackgroundColor(Color.WHITE)
            setPadding(16, 16, 16, 16)
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )

            setOnClickListener { onNumberClicked(this) }

            binding.gameContainer.post {
                val containerWidth = binding.gameContainer.width
                val containerHeight = binding.gameContainer.height

                val randomX = Random.nextInt(containerWidth - width).coerceAtLeast(0)
                val randomY = Random.nextInt(containerHeight - height).coerceAtLeast(0)

                x = randomX.toFloat()
                y = randomY.toFloat()
            }
        }
    }

    private fun startMovingViews() {
        movingViews.forEach { view ->
            animateView(view)
        }
    }

    private fun onNumberClicked(textView: TextView) {
        if (!textView.isClickable) return // Already matched, ignore

        // Deselecting view
        if (selectedTextView == textView) {
            textView.setBackgroundColor(Color.WHITE)
            selectedTextView = null
            return
        }

        val isRoman = textView.text.matches(Regex("^[IVXLCDM]+$"))
        val isFirstRoman = selectedTextView?.text?.matches(Regex("^[IVXLCDM]+$")) ?: !isRoman

        if (selectedTextView == null) {
            selectedTextView = textView
            textView.setBackgroundColor(Color.LTGRAY) // Highlight selection
        } else if (isFirstRoman == isRoman) {
            Toast.makeText(requireContext(), "Cannot select two numbers of the same type", Toast.LENGTH_SHORT).show()
        } else {
            val first = selectedTextView!!
            if (romanArabicPairs[first] == textView) {
                // Correct match
                first.isClickable = false
                textView.isClickable = false
                val customGreen = ContextCompat.getColor(requireContext(), R.color.green_pastel)
                first.setBackgroundColor(customGreen)
                textView.setBackgroundColor(customGreen)

                matchedPairs++
                if (matchedPairs == romanArabicPairs.size / 2) {
                    round++
                    successfulRounds++
                    handler.postDelayed({ startNewRound() }, 1000)
                }
            } else {
                // Incorrect match
                first.setBackgroundColor(Color.WHITE)
                Toast.makeText(requireContext(), "Incorrect match!", Toast.LENGTH_SHORT).show()
            }
            selectedTextView = null
        }
    }

    private fun animateView(view: TextView) {
        binding.gameContainer.post {
            val containerWidth = binding.gameContainer.width
            val containerHeight = binding.gameContainer.height

            val randomX = Random.nextInt(0, containerWidth - view.width).toFloat()
            val randomY = Random.nextInt(0, containerHeight - view.height).toFloat()

            val animX = ObjectAnimator.ofFloat(view, "x", view.x, randomX).apply {
                duration = (1000..4000).random().toLong()
                repeatCount = ObjectAnimator.INFINITE
                repeatMode = ObjectAnimator.REVERSE
            }

            val animY = ObjectAnimator.ofFloat(view, "y", view.y, randomY).apply {
                duration = (1000..4000).random().toLong()
                repeatCount = ObjectAnimator.INFINITE
                repeatMode = ObjectAnimator.REVERSE
            }

            AnimatorSet().apply {
                playTogether(animX, animY)
                start()
            }
        }
    }

    private fun displayFinalScore() {
        val gameName = requireActivity().intent.getStringExtra("GAME_NAME") ?: "Default Game"
        val score = GameDifficultyUtils.calculateScore(gameName, successfulRounds)

        val scoreLayout = ScoreUtility.createScoreLayout(requireContext(), score) { _ ->
        }

        AlertDialog.Builder(requireContext())
            .setTitle("Game Over")
            .setView(scoreLayout)
            .setMessage("You solved $successfulRounds rounds!")
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