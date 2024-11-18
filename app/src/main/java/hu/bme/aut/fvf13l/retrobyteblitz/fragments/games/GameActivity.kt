package hu.bme.aut.fvf13l.retrobyteblitz.fragments.games

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import hu.bme.aut.fvf13l.retrobyteblitz.R
import hu.bme.aut.fvf13l.retrobyteblitz.fragments.menus.TopCategoryFragment
import hu.bme.aut.fvf13l.retrobyteblitz.utility.CameraHelper

class GameActivity : AppCompatActivity(), CountdownTimerFragment.TimerEndListener {
    private var timeLimit: Long = 60000L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)

        // Get data from the intent or other sources
        val gameName = intent.getStringExtra("GAME_NAME") ?: "Unknown Game"
        timeLimit = intent.getLongExtra("TIME_LIMIT", 60000L)

        // Set up the top container (e.g., title or game name display)
        supportFragmentManager.beginTransaction()
            .replace(R.id.topGameFragmentContainer, TopCategoryFragment.newInstance(gameName))
            .commit()

        launchGame(gameName)
    }

    private fun launchGame(gameName: String) {
        if (gameName == "Slider") {
            // Delay the timer for Slider game
            promptForPhoto()
        } else {
            // For other games, immediately start the countdown timer
            val gameFragment = getGameFragment(gameName)
            gameFragment?.let {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.middleGameFragmentContainer, it)
                    .commit()
            }

            // Immediately add the countdown timer fragment for other games
            supportFragmentManager.beginTransaction()
                .replace(R.id.bottomGameFragmentContainer, CountdownTimerFragment.newInstance(timeLimit))
                .commit()
        }
    }


    // Get the fragment instance for a given game
    private fun getGameFragment(gameName: String): Fragment? {
        return when (gameName) {
            "Sudoku" -> SudokuGameFragment()
            "Calculation" -> CalculationGameFragment()
            "MemoryGame" -> MemoryGameFragment()
            "Grid" -> GridMemoryGameFragment()
            "Sequence" -> SequenceGameFragment()
            "Number Of" -> NumberOfGameFragment()
            "Moving Sum" -> MovingSumGameFragment()
            "Descending" -> DescendingGameFragment()
            "Card" -> MemoryCardGameFragment()
            "Slider" -> SlidingGameFragment()
            else -> null
        }
    }

    // Prompt the user for photo or default image
    private fun promptForPhoto() {
        val cameraHelper = CameraHelper(this)

        // Show dialog for photo selection (immediately)
        val options = arrayOf("Take Photo", "Use Default Image")
        AlertDialog.Builder(this)
            .setTitle("Choose Image")
            .setItems(options) { dialog, which ->
                when (which) {
                    0 -> {
                        cameraHelper.launchCamera() // No need to request, proceed directly
                    }
                    1 -> {
                        // Use default image, no need to request permissions
                        launchSliderGameWithImage(null)
                    }
                }
            }
            .show()
    }

    // Handle the result of the camera activity
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == CameraHelper.REQUEST_CAMERA) {
            if (resultCode == RESULT_OK) {
                // Retrieve the image directly from the intent's data
                val imageBitmap: Bitmap? = data?.extras?.get("data") as? Bitmap

                imageBitmap?.let { bitmap ->
                    // Use the bitmap in the puzzle game
                    launchSliderGameWithImage(bitmap)
                } ?: run {
                    Toast.makeText(this, "Failed to capture photo", Toast.LENGTH_SHORT).show()
                }
            } else {
                // If the result is not OK, finish the activity
                Toast.makeText(this, "Camera result not OK. Closing activity.", Toast.LENGTH_SHORT).show()
                finish()  // Finish the activity and return to the previous one
            }
        }
    }

    // In your Activity or Fragment:
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == CameraHelper.REQUEST_CAMERA_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, launch the camera
                val cameraHelper = CameraHelper(this)
                cameraHelper.launchCamera()
                Toast.makeText(this, "Permission granted.", Toast.LENGTH_SHORT).show()
            } else {
                // Permission denied, show a toast
                Toast.makeText(this, "Camera permission denied. Can't access the camera.", Toast.LENGTH_SHORT).show()
            }
        }
    }


    // Launch the slider game with the selected image
    private fun launchSliderGameWithImage(image: Bitmap?) {
        // Create the Slider game fragment
        val gameFragment = SlidingGameFragment()
        val bundle = Bundle()
        bundle.putParcelable("image", image) // Pass the Bitmap as a parcelable
        gameFragment.arguments = bundle

        // Add the Slider game fragment
        supportFragmentManager.beginTransaction()
            .replace(R.id.middleGameFragmentContainer, gameFragment)
            .commit()

        // Add the countdown timer fragment after the photo is selected
        supportFragmentManager.beginTransaction()
            .replace(R.id.bottomGameFragmentContainer, CountdownTimerFragment.newInstance(timeLimit))
            .commit()
    }


    override fun onTimerEnd() {
        val gameFragment = supportFragmentManager.findFragmentById(R.id.middleGameFragmentContainer) as? CountdownTimerFragment.TimerEndListener
        gameFragment?.onTimerEnd()
    }
}
