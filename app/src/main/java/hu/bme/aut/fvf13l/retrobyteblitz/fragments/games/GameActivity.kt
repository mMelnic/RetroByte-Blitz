package hu.bme.aut.fvf13l.retrobyteblitz.fragments.games

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import hu.bme.aut.fvf13l.retrobyteblitz.R
import hu.bme.aut.fvf13l.retrobyteblitz.fragments.menus.TopCategoryFragment

class GameActivity : AppCompatActivity(), CountdownTimerFragment.TimerEndListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)

        // Get data from the intent or other sources
        val gameName = intent.getStringExtra("GAME_NAME") ?: "Unknown Game"
        val timeLimit = intent.getLongExtra("TIME_LIMIT", 60000L)

        // Set up the top container (e.g., title or game name display)
        supportFragmentManager.beginTransaction()
            .replace(R.id.topGameFragmentContainer, TopCategoryFragment.newInstance(gameName))
            .commit()

        launchGame(gameName)

        // Set up the bottom container with the countdown timer fragment
        supportFragmentManager.beginTransaction()
            .replace(R.id.bottomGameFragmentContainer, CountdownTimerFragment.newInstance(timeLimit))
            .commit()
    }

    private fun launchGame(gameName: String) {
        val gameFragment = when (gameName) {
            "Sudoku" -> SudokuGameFragment()
            "Calculation" -> CalculationGameFragment()
            "MemoryGame" -> MemoryGameFragment()
            "Drag" -> MemoryDragGameFragment()
            "Grid" -> GridMemoryGameFragment()
            "Sequence" -> SequenceGameFragment()
            "Number Of" -> NumberOfGameFragment()
            "Moving Sum" -> MovingSumGameFragment()
            // Add other cases for additional games as needed
            else -> null
        }

        gameFragment?.let {
            supportFragmentManager.beginTransaction()
                .replace(R.id.middleGameFragmentContainer, it)
                .commit()
        }
    }

    override fun onTimerEnd() {
        val gameFragment = supportFragmentManager.findFragmentById(R.id.middleGameFragmentContainer) as? CountdownTimerFragment.TimerEndListener
        gameFragment?.onTimerEnd()
    }
}
