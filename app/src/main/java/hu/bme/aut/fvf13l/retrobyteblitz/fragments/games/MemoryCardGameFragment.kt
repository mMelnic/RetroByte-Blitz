package hu.bme.aut.fvf13l.retrobyteblitz.fragments.games

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.gridlayout.widget.GridLayout
import hu.bme.aut.fvf13l.retrobyteblitz.R
import hu.bme.aut.fvf13l.retrobyteblitz.databinding.FragmentMemoryCardGameBinding
import hu.bme.aut.fvf13l.retrobyteblitz.utility.GameDifficultyUtils
import hu.bme.aut.fvf13l.retrobyteblitz.utility.ScoreUtility

class MemoryCardGameFragment : Fragment(), CountdownTimerFragment.TimerEndListener {

    private lateinit var binding: FragmentMemoryCardGameBinding
    private var cards = mutableListOf<Card>()
    private var flippedCards = mutableListOf<Card>()
    private var solvedRounds = 0

    private data class Card(val id: Int, val imageResId: Int, var isFlipped: Boolean = false)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentMemoryCardGameBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Start the game
        startNewGame()
    }

    private fun startNewGame() {
        cards.clear()
        flippedCards.clear()
        binding.gridLayout.removeAllViews()

        // Create the cards (pairs of retroItem1 to retroItem8)
        val cardImages = listOf(
            R.drawable.retro_item1, R.drawable.retro_item2, R.drawable.retro_item3,
            R.drawable.retro_item4, R.drawable.retro_item5, R.drawable.retro_item6,
            R.drawable.retro_item7, R.drawable.retro_item8
        )

        // Duplicate the images to create pairs
        val cardList = mutableListOf<Card>()
        for (imageResId in cardImages) {
            cardList.add(Card(id = imageResId, imageResId = imageResId))
            cardList.add(Card(id = imageResId, imageResId = imageResId))
        }

        // Shuffle the cards to randomize their positions
        cardList.shuffle()

        // Add the cards to the grid layout
        for (card in cardList) {
            val cardView = ImageView(requireContext())
            cardView.scaleType = ImageView.ScaleType.CENTER_CROP  // Back card (cover the ImageView entirely)
            cardView.setImageResource(R.drawable.card_back) // The back image for the cards
            cardView.layoutParams = GridLayout.LayoutParams().apply {
                width = 200
                height = 300
                setMargins(15, 15, 15, 15)
            }

            // Set an onClickListener for each card
            cardView.setOnClickListener {
                if (!card.isFlipped && flippedCards.size < 2) {
                    flipCard(card, cardView)
                }
            }

            // Add the card view to the grid layout
            binding.gridLayout.addView(cardView)
            cards.add(card)
        }
    }

    private fun flipCard(card: Card, cardView: ImageView) {
        // Show the card's image
        card.isFlipped = true
        cardView.scaleType = ImageView.ScaleType.FIT_CENTER
        cardView.setImageResource(card.imageResId)
        flippedCards.add(card)

        // Check if two cards are flipped
        if (flippedCards.size == 2) {
            // Delay to give the player time to see the second flipped card
            Handler(Looper.getMainLooper()).postDelayed({
                checkForMatch()
            }, 500) // Wait 1 second before checking if the cards match
        }
    }

    private fun checkForMatch() {
        val firstCard = flippedCards[0]
        val secondCard = flippedCards[1]

        if (firstCard.id != secondCard.id) {
            // Cards do not match, flip them back
            val firstView = binding.gridLayout.getChildAt(cards.indexOf(firstCard)) as ImageView
            val secondView = binding.gridLayout.getChildAt(cards.indexOf(secondCard)) as ImageView

            firstCard.isFlipped = false
            secondCard.isFlipped = false

            // Delay to show the second card before flipping them back
            Handler(Looper.getMainLooper()).postDelayed({
                firstView.scaleType = ImageView.ScaleType.CENTER_CROP
                secondView.scaleType = ImageView.ScaleType.CENTER_CROP
                firstView.setImageResource(R.drawable.card_back)
                secondView.setImageResource(R.drawable.card_back)
            }, 300) // Flip back after half a second
        }

        flippedCards.clear()

        // Check if all pairs are matched, start a new game
        if (cards.all { it.isFlipped }) {
            solvedRounds++
            updateScore()
            Handler(Looper.getMainLooper()).postDelayed({
                startNewGame()
            }, 500) // Wait 1 second after all pairs are matched before restarting
        }
    }

    private fun updateScore() {
        binding.scoreTextView.text = "Score: $solvedRounds"
    }

    private fun displayFinalScore() {
        val gameName = requireActivity().intent.getStringExtra("GAME_NAME") ?: "Default Game"
        val score = GameDifficultyUtils.calculateScore(gameName, solvedRounds)

        val scoreLayout = ScoreUtility.createScoreLayout(requireContext(), score) { _ ->
        }

        AlertDialog.Builder(requireContext())
            .setTitle("Game Over")
            .setView(scoreLayout)
            .setMessage("You solved $solvedRounds rounds!")
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
