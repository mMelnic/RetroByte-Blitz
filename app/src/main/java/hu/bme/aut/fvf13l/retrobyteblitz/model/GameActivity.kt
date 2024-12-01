package hu.bme.aut.fvf13l.retrobyteblitz.model

import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import hu.bme.aut.fvf13l.retrobyteblitz.R
import hu.bme.aut.fvf13l.retrobyteblitz.fragments.games.CalculationGameFragment
import hu.bme.aut.fvf13l.retrobyteblitz.fragments.games.CountdownTimerFragment
import hu.bme.aut.fvf13l.retrobyteblitz.fragments.games.DescendingGameFragment
import hu.bme.aut.fvf13l.retrobyteblitz.fragments.games.GridMemoryGameFragment
import hu.bme.aut.fvf13l.retrobyteblitz.fragments.games.MemoryCardGameFragment
import hu.bme.aut.fvf13l.retrobyteblitz.fragments.games.MemoryGameFragment
import hu.bme.aut.fvf13l.retrobyteblitz.fragments.games.MovingSumGameFragment
import hu.bme.aut.fvf13l.retrobyteblitz.fragments.games.NumberOfGameFragment
import hu.bme.aut.fvf13l.retrobyteblitz.fragments.games.RomanNumeralsGameFragment
import hu.bme.aut.fvf13l.retrobyteblitz.fragments.games.SequenceGameFragment
import hu.bme.aut.fvf13l.retrobyteblitz.fragments.games.SlidingGameFragment
import hu.bme.aut.fvf13l.retrobyteblitz.fragments.games.StroopGameFragment
import hu.bme.aut.fvf13l.retrobyteblitz.fragments.games.SudokuGameFragment
import hu.bme.aut.fvf13l.retrobyteblitz.fragments.menus.TopCategoryFragment
import hu.bme.aut.fvf13l.retrobyteblitz.service.CameraService

class GameActivity : AppCompatActivity(), CountdownTimerFragment.TimerEndListener {
    private var timeLimit: Long = 60000L
    private var cameraService: CameraService? = null
    private var isServiceBound = false
    private val pendingActions = mutableListOf<() -> Unit>()
    private val serviceTimeout: Long = 10000L
    private var serviceTimeoutHandler: Handler? = null

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as CameraService.CameraBinder
            cameraService = binder.getService()
            isServiceBound = true
            serviceTimeoutHandler?.removeCallbacksAndMessages(null)
            pendingActions.forEach { it.invoke() }
            pendingActions.clear()
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            cameraService = null
            isServiceBound = false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)

        Intent(this, CameraService::class.java).also { intent ->
            bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
        }

        val gameName = intent.getStringExtra("GAME_NAME") ?: "Unknown Game"
        timeLimit = intent.getLongExtra("TIME_LIMIT", 60000L)

        supportFragmentManager.beginTransaction()
            .replace(R.id.topGameFragmentContainer, TopCategoryFragment.newInstance(gameName))
            .commit()

        launchGame(gameName)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isServiceBound) {
            unbindService(serviceConnection)
            isServiceBound = false
        }
        serviceTimeoutHandler?.removeCallbacksAndMessages(null)
    }

    private fun launchGame(gameName: String) {
        if (gameName == "Slider") {
            promptForPhoto()
        } else {
            val gameFragment = getGameFragment(gameName)
            gameFragment?.let {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.middleGameFragmentContainer, it)
                    .commit()
            }

            supportFragmentManager.beginTransaction()
                .replace(R.id.bottomGameFragmentContainer,
                    CountdownTimerFragment.newInstance(timeLimit)
                )
                .commit()
        }
    }

    private fun getGameFragment(gameName: String): Fragment? {
        return when (gameName) {
            "Sudoku" -> SudokuGameFragment()
            "Calculation" -> CalculationGameFragment()
            "Colors" -> MemoryGameFragment()
            "Grid" -> GridMemoryGameFragment()
            "Sequence" -> SequenceGameFragment()
            "Number Of" -> NumberOfGameFragment()
            "Moving Sum" -> MovingSumGameFragment()
            "Descending" -> DescendingGameFragment()
            "Card" -> MemoryCardGameFragment()
            "Stroop" -> StroopGameFragment()
            "Roman Gladiator" -> RomanNumeralsGameFragment()
            else -> null
        }
    }

    private fun promptForPhoto() {
        if (isServiceBound) {
            val options = arrayOf("Take Photo", "Use Default Image")
            AlertDialog.Builder(this)
                .setTitle("Choose Image")
                .setItems(options) { dialog, which ->
                    when (which) {
                        0 -> cameraService?.launchCamera(this)
                        1 -> launchSliderGameWithImage(null)
                    }
                }
                .show()
        } else {
            pendingActions.add { promptForPhoto() }
            startServiceTimeout()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == CameraService.REQUEST_CAMERA && resultCode == Activity.RESULT_OK) {
            val imageBitmap: Bitmap? = data?.extras?.get("data") as? Bitmap
            imageBitmap?.let { bitmap ->
                launchSliderGameWithImage(bitmap)
            } ?: run {
                Toast.makeText(this, "Failed to capture photo", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "Camera result not OK. Closing activity.", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == CameraService.REQUEST_CAMERA_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                cameraService?.launchCamera(this)
                Toast.makeText(this, "Permission granted.", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Camera permission denied.", Toast.LENGTH_SHORT).show()
                launchSliderGameWithImage(null)
            }
        }
    }

    private fun launchSliderGameWithImage(image: Bitmap?) {
        val gameFragment = SlidingGameFragment()
        val bundle = Bundle()
        bundle.putParcelable("image", image)
        gameFragment.arguments = bundle

        supportFragmentManager.beginTransaction()
            .replace(R.id.middleGameFragmentContainer, gameFragment)
            .commit()

        supportFragmentManager.beginTransaction()
            .replace(R.id.bottomGameFragmentContainer,
                CountdownTimerFragment.newInstance(timeLimit)
            )
            .commit()
    }

    private fun startServiceTimeout() {
        serviceTimeoutHandler = Handler(mainLooper)

        serviceTimeoutHandler?.postDelayed({
            if (!isServiceBound) {
                Toast.makeText(this, "Camera service timed out. Using default image.", Toast.LENGTH_SHORT).show()
                pendingActions.clear()
                launchSliderGameWithImage(null)
            }
        }, serviceTimeout)
    }

    override fun onTimerEnd() {
        val gameFragment = supportFragmentManager.findFragmentById(R.id.middleGameFragmentContainer) as? CountdownTimerFragment.TimerEndListener
        gameFragment?.onTimerEnd()
    }
}
