package hu.bme.aut.fvf13l.retrobyteblitz.fragments.games

import android.app.Activity
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.RippleDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import hu.bme.aut.fvf13l.retrobyteblitz.databinding.FragmentMemoryGameBinding
import kotlin.random.Random

class MemoryGameFragment : Fragment(), CountdownTimerFragment.TimerEndListener {

    private lateinit var binding: FragmentMemoryGameBinding

    private val colors = listOf(
        Color.RED,
        Color.GREEN,
        Color.BLUE,
        Color.YELLOW,
        Color.CYAN,
        Color.MAGENTA,
        Color.LTGRAY
    )

    private var currentSequence = mutableListOf<Int>()
    private var userSequence = mutableListOf<Int>()
    private var round = 1
    private var successfulRounds = 0
    private var totalRounds = 0
    private val handler = Handler(Looper.getMainLooper())

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentMemoryGameBinding.inflate(inflater, container, false)
        setupButtons()
        startNewRound()
        return binding.root
    }

    private fun setupButtons() {
        for (i in colors.indices) {
            val button = Button(context)
            button.setBackgroundColor(colors[i])
            button.tag = i
            button.setOnClickListener { onColorSelected(i) }
            val rippleDrawable = RippleDrawable(
                ColorStateList.valueOf(Color.GRAY),
                ColorDrawable(colors[i]),
                null
            )
            button.background = rippleDrawable
            val params = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            button.layoutParams = params
            binding.buttonsContainer.addView(button)
        }
    }

    private fun startNewRound() {
        setButtonsEnabled(false)
        currentSequence.clear()
        userSequence.clear()
        binding.userSequenceContainer.removeAllViews()
        for (i in 1..round + 1) {
            currentSequence.add(Random.nextInt(colors.size))
        }
        binding.roundInfo.text = "Round $round"
        handler.postDelayed({
            displaySequence {
                // Enabling buttons only after the sequence has finished
                setButtonsEnabled(true)
            }
        }, 700)
    }

    private fun setButtonsEnabled(enabled: Boolean) {
        for (i in 0 until binding.buttonsContainer.childCount) {
            val view = binding.buttonsContainer.getChildAt(i)
            if (view is Button) {
                view.isEnabled = enabled
            }
        }
    }

    private fun displaySequence(onSequenceFinished: () -> Unit) {
        var index = 0
        handler.postDelayed(object : Runnable {
            override fun run() {
                if (index < currentSequence.size) {
                    binding.colorDisplay.setBackgroundColor(colors[currentSequence[index]])
                    handler.postDelayed({
                        binding.colorDisplay.setBackgroundColor(Color.WHITE)
                        index++
                        handler.postDelayed(this, 500)
                    }, 900)
                } else {
                    onSequenceFinished()
                }
            }
        }, 200)
    }


    private fun onColorSelected(colorIndex: Int) {
        if (userSequence.size < currentSequence.size) {
            userSequence.add(colorIndex)
            addColorToSequence(colorIndex)

            if (userSequence[userSequence.size - 1] != currentSequence[userSequence.size - 1]) {
                totalRounds++
                binding.scoreInfo.text = "Score: $successfulRounds/$totalRounds"
                round = 1
                handler.postDelayed({
                    startNewRound()
                }, 1000)
            } else if (userSequence.size == currentSequence.size) {
                successfulRounds++
                totalRounds++
                binding.scoreInfo.text = "Score: $successfulRounds/$totalRounds"
                round++
                handler.postDelayed({
                    startNewRound()
                }, 1000)
            }
        } else {
            Log.d("MemoryGame", "User pressed too many buttons!")
        }
    }


    private fun addColorToSequence(colorIndex: Int) {
        val view = View(context)
        view.setBackgroundColor(colors[colorIndex])
        val params = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1f)
        params.setMargins(2, 2, 2, 2) // Thin lines as dividers
        view.layoutParams = params
        binding.userSequenceContainer.addView(view)
    }

    override fun onTimerEnd() {
        AlertDialog.Builder(requireContext())
            .setTitle("Game Over")
            .setMessage("Score: $successfulRounds/$totalRounds")
            .setPositiveButton("OK") { _, _ -> sendResultAndFinish() }
            .setOnDismissListener { sendResultAndFinish() }
            .show()
    }

    private fun sendResultAndFinish() {
        val resultIntent = Intent().apply {
            putExtra("SUCCESSFUL_ROUNDS", successfulRounds)
        }
        activity?.setResult(Activity.RESULT_OK, resultIntent)
        activity?.finish()
    }
}
