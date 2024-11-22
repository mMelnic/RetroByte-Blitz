package hu.bme.aut.fvf13l.retrobyteblitz.utility

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Color
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView

object ScoreUtility {

    // Method to create the layout with label and animated score TextView
    fun createScoreLayout(context: Context, score: Int, onAnimationUpdate: (Int) -> Unit): LinearLayout {
        // Create a LinearLayout to hold the label and animated score
        return LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER // Center content inside the layout
            setPadding(40, 40, 40, 40)  // Optional padding for better spacing

            // Create the label "Score"
            val scoreLabel = TextView(context).apply {
                text = "Score"
                textSize = 24f
                setTextColor(Color.BLACK)
                gravity = Gravity.CENTER
            }

            // Create the animated score TextView
            val scoreTextView = TextView(context).apply {
                text = "0"  // Initial value
                textSize = 48f  // Make the score text larger
                setTextColor(Color.BLACK)
                gravity = Gravity.CENTER
            }

            // Add the label and score text to the layout
            addView(scoreLabel)
            addView(scoreTextView)

            // Set up the ValueAnimator to animate the score
            val animator = ValueAnimator.ofInt(0, score).apply {
                duration = 2000  // Animation duration
                addUpdateListener { animation ->
                    // Update the score text during animation
                    val animatedValue = animation.animatedValue as Int
                    scoreTextView.text = animatedValue.toString()
                    // Call the callback with the animated value
                    onAnimationUpdate(animatedValue)
                }
            }

            // Start the animation immediately after layout is set up
            animator.start()
        }
    }
}
