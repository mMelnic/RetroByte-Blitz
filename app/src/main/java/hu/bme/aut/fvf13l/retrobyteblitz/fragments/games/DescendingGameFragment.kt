package hu.bme.aut.fvf13l.retrobyteblitz.fragments.games

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import hu.bme.aut.fvf13l.retrobyteblitz.R
import hu.bme.aut.fvf13l.retrobyteblitz.databinding.FragmentDescendingGameBinding
import hu.bme.aut.fvf13l.retrobyteblitz.utility.GameDifficultyUtils
import hu.bme.aut.fvf13l.retrobyteblitz.utility.ScoreUtility
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.random.Random

class DescendingGameFragment : Fragment(), CountdownTimerFragment.TimerEndListener {

    private lateinit var binding: FragmentDescendingGameBinding
    private val characters = listOf(R.drawable.pilot, R.drawable.doctor, R.drawable.engineer)
    private val handler = Handler(Looper.getMainLooper())
    private val userAnswer = mutableListOf<Int>()
    private lateinit var gridImages: List<Int>
    private val gameScope = CoroutineScope(Dispatchers.Main)
    private var totalRounds = 0
    private var solvedRounds = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentDescendingGameBinding.inflate(inflater, container, false)
        startNewRound()

        setupImageButtonListeners()

        return binding.root
    }

    private fun startNewRound() {
        totalRounds++

        // Calculate the flash duration based on user progress
        val baseFlashTime = 3000 // Initial flash time in milliseconds (e.g., 3 seconds)
        val progressFactor = 200 // Decrease time by this value per round
        val minFlashTime = 1000 // Minimum flash time to maintain playability

        val flashTime = (baseFlashTime - (solvedRounds * progressFactor)).coerceAtLeast(minFlashTime)

        setButtonsEnabled(false)
        gameScope.launch {
            // Generate counts on a background thread
            val counts = withContext(Dispatchers.Default) {
                generateDistinctCounts(8)
            }

            val imagesWithOccurrences = mutableListOf<Int>()
            characters.forEachIndexed { index, character ->
                repeat(counts[index]) {
                    imagesWithOccurrences.add(character)
                }
            }

            // Shuffle the list to randomize the grid order
            gridImages = imagesWithOccurrences.shuffled()

            val imageViews = listOf(
                binding.imageView1, binding.imageView2, binding.imageView3, binding.imageView4,
                binding.imageView5, binding.imageView6, binding.imageView7, binding.imageView8
            )

            imageViews.forEachIndexed { index, imageView ->
                imageView.setImageResource(gridImages[index])
            }

            // Flash the grid for 3 seconds
            handler.postDelayed({
                imageViews.forEach { it.setImageResource(0) }  // Clear the images
                setButtonsEnabled(true)
            }, flashTime.toLong())
        }
    }

    private fun setButtonsEnabled(enabled: Boolean) {
        binding.pilotButton.isEnabled = enabled
        binding.doctorButton.isEnabled = enabled
        binding.engineerButton.isEnabled = enabled
    }

    private fun generateDistinctCounts(totalCells: Int): List<Int> {
        val num1 = Random.nextInt(1, totalCells) // Ensure at least 1 appearance
        var num2: Int
        do {
            num2 = Random.nextInt(1, totalCells - num1 + 1)
        } while (num2 == num1)

        var num3 = totalCells - num1 - num2
        while (num3 == num1 || num3 == num2 || num3 < 0) {
            do {
                num2 = Random.nextInt(1, totalCells - num1 + 1)
            } while (num2 == num1)

            num3 = totalCells - num1 - num2
        }

        return listOf(num1, num2, num3).shuffled()
    }


    private fun setupImageButtonListeners() {
        binding.pilotButton.setOnClickListener { onImageSelected(R.drawable.pilot) }
        binding.doctorButton.setOnClickListener { onImageSelected(R.drawable.doctor) }
        binding.engineerButton.setOnClickListener { onImageSelected(R.drawable.engineer) }
    }

    private fun onImageSelected(imageRes: Int) {
        if (userAnswer.size < 3) {
            userAnswer.add(imageRes)
            displayUserAnswer()

            if (userAnswer.size == 3) {
                checkUserAnswer()
            }
        }
    }

    private fun displayUserAnswer() {
        val answerViews = listOf(binding.answerView1, binding.answerView2, binding.answerView3)

        userAnswer.forEachIndexed { index, imageRes ->
            answerViews[index].setImageResource(imageRes)
        }
    }

    private fun checkUserAnswer() {
        // Initialize count map with all characters set to zero
        val allCounts = characters.associateWith { 0 }.toMutableMap()

        // Update counts with actual occurrences from the grid
        gridImages.groupingBy { it }.eachCount().forEach { (key, value) ->
            allCounts[key] = value
        }

        // Sort the characters by count in descending order
        val countMap = allCounts.entries.sortedByDescending { it.value }
        val correctAnswer = countMap.map { it.key }

        // Check if the user's answer matches the top three characters by count
        if (userAnswer == correctAnswer.take(3)) {
            solvedRounds++ // Increment solved rounds if the answer is correct
            Toast.makeText(requireContext(), "Correct!", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(requireContext(), "Incorrect. Try again!", Toast.LENGTH_SHORT).show()
        }
        updateScore()

        // Start a new round after a brief delay
        handler.postDelayed({
            userAnswer.clear()
            binding.answerView1.setImageResource(0)
            binding.answerView2.setImageResource(0)
            binding.answerView3.setImageResource(0)
            startNewRound()
        }, 1000)
    }
    private fun updateScore() {
        binding.scoreTextView.text = "Correct: $solvedRounds / $totalRounds"
    }

    private fun displayFinalScore() {
        var gameName = requireActivity().intent.getStringExtra("GAME_NAME") ?: "Default Game"
        val score = GameDifficultyUtils.calculateScore(gameName, solvedRounds)

        val scoreLayout = ScoreUtility.createScoreLayout(requireContext(), score) { _ ->
        }

        AlertDialog.Builder(requireContext())
            .setTitle("Game Over")
            .setView(scoreLayout)
            .setMessage("You solved $solvedRounds out of $totalRounds rounds!")
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

