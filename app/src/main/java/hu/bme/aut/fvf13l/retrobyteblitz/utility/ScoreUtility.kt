package hu.bme.aut.fvf13l.retrobyteblitz.utility

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Color
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView

object ScoreUtility {

    fun createScoreLayout(context: Context, score: Int, onAnimationUpdate: (Int) -> Unit): LinearLayout {
        return LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(40, 40, 40, 40)

            val scoreLabel = TextView(context).apply {
                text = "Score"
                textSize = 24f
                setTextColor(Color.BLACK)
                gravity = Gravity.CENTER
            }

            val scoreTextView = TextView(context).apply {
                text = "0"
                textSize = 48f
                setTextColor(Color.BLACK)
                gravity = Gravity.CENTER
            }
            addView(scoreLabel)
            addView(scoreTextView)

            val animator = ValueAnimator.ofInt(0, score).apply {
                duration = 2000
                addUpdateListener { animation ->
                    val animatedValue = animation.animatedValue as Int
                    scoreTextView.text = animatedValue.toString()
                    onAnimationUpdate(animatedValue)
                }
            }
            animator.start()
        }
    }
}
