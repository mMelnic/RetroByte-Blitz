package hu.bme.aut.fvf13l.retrobyteblitz.fragments.games

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
import androidx.core.view.children
import androidx.fragment.app.Fragment
import hu.bme.aut.fvf13l.retrobyteblitz.databinding.FragmentRomanNumeralsGameBinding
import kotlin.random.Random

class RomanNumeralsGameFragment : Fragment() {

    private lateinit var binding: FragmentRomanNumeralsGameBinding

    private val handler = Handler(Looper.getMainLooper())
    private val movingViews = mutableListOf<TextView>()
    private var selectedTextView: TextView? = null

    private var matchedPairs = 0
    private var round = 1
    private val romanArabicPairs = mutableMapOf<TextView, TextView>()

    private fun generateRomanNumeral(level: Int, usedValues: MutableSet<Int>): Pair<String, Int> {
        val maxValue = when (level) {
            1 -> 10 // Level 1: Roman numerals between 1-10
            2 -> 50 // Level 2: Roman numerals between 1-50
            else -> 100 // Level 3 and beyond: 1-100
        }

        var value: Int
        do {
            value = Random.nextInt(1, maxValue + 1) // Avoid duplicate pairs
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
        matchedPairs = 0

        val pairsCount = 2 + round // Start with 3 pairs and increase with each round
        val usedValues = mutableSetOf<Int>()

        for (i in 1..pairsCount) {
            val (roman, value) = generateRomanNumeral(round, usedValues)

            // Create Roman numeral TextView
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

            // Assign random velocity (dx, dy) and store in tag
            val randomDx = Random.nextFloat() * 10 - 5 // Random velocity between -5 and 5
            val randomDy = Random.nextFloat() * 10 - 5
            tag = Pair(randomDx, randomDy) // Save velocity in tag

            // Position the text view using post to ensure container dimensions are available
            post {
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
        val moveRunnable = object : Runnable {
            override fun run() {
                binding.gameContainer.children.forEach { child ->
                    if (child is TextView) {
                        moveView(child)
                    }
                }
                handler.postDelayed(this, 16) // ~60 FPS
            }
        }
        handler.post(moveRunnable)
    }

    private fun onNumberClicked(textView: TextView) {
        if (!textView.isClickable) return // Already matched, ignore

        // If the same view is clicked twice, deselect it
        if (selectedTextView == textView) {
            textView.setBackgroundColor(Color.WHITE)
            selectedTextView = null
            return
        }

        // Check if the selected views are of the same type (both Roman or both Arabic)
        val isRoman = textView.text.matches(Regex("^[IVXLCDM]+$")) // Roman numeral regex
        val isFirstRoman = selectedTextView?.text?.matches(Regex("^[IVXLCDM]+$")) ?: !isRoman

        if (selectedTextView == null) {
            // First selection
            selectedTextView = textView
            textView.setBackgroundColor(Color.LTGRAY) // Highlight selection
        } else if (isFirstRoman == isRoman) {
            // Prevent selecting two of the same type
            Toast.makeText(requireContext(), "Cannot select two of the same type", Toast.LENGTH_SHORT).show()
        } else {
            // Check if the pair matches
            val first = selectedTextView!!
            if (romanArabicPairs[first] == textView) {
                // Correct match
                first.isClickable = false
                textView.isClickable = false
                first.setBackgroundColor(Color.GREEN)
                textView.setBackgroundColor(Color.GREEN)

                matchedPairs++
                if (matchedPairs == romanArabicPairs.size / 2) {
                    round++
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

    private fun moveView(view: TextView) {
        val currentX = view.x
        val currentY = view.y

        val velocity = view.tag as Pair<Float, Float>
        var dx = velocity.first
        var dy = velocity.second

        val containerWidth = binding.gameContainer.width
        val containerHeight = binding.gameContainer.height
        val viewWidth = view.width
        val viewHeight = view.height

        if (currentX + dx <= 0 || currentX + dx + viewWidth >= containerWidth) dx = -dx
        if (currentY + dy <= 0 || currentY + dy + viewHeight >= containerHeight) dy = -dy

        view.x = (currentX + dx).coerceIn(0f, (containerWidth - viewWidth).toFloat())
        view.y = (currentY + dy).coerceIn(0f, (containerHeight - viewHeight).toFloat())

        view.tag = Pair(dx, dy)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        handler.removeCallbacksAndMessages(null)
    }
}

