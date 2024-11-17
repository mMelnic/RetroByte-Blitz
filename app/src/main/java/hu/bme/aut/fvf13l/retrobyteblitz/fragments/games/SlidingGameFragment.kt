package hu.bme.aut.fvf13l.retrobyteblitz.fragments.games

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.fragment.app.Fragment
import hu.bme.aut.fvf13l.retrobyteblitz.R
import hu.bme.aut.fvf13l.retrobyteblitz.databinding.FragmentSlidingGameBinding
import android.view.ViewTreeObserver
import androidx.appcompat.app.AlertDialog

class SlidingGameFragment : Fragment(), CountdownTimerFragment.TimerEndListener {

    private lateinit var binding: FragmentSlidingGameBinding
    private lateinit var puzzleGrid: Array<Array<ImageView?>>
    private lateinit var originalImage: Bitmap
    private var gridSize = 3
    private var selectedTiles = mutableListOf<Pair<Int, Int>>()
    private var solvedGames = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSlidingGameBinding.inflate(inflater, container, false)
        initializeGame()
        return binding.root
    }

    private fun initializeGame() {
        puzzleGrid = Array(gridSize) { arrayOfNulls(gridSize) }

        binding.puzzleContainer.viewTreeObserver.addOnPreDrawListener(object : ViewTreeObserver.OnPreDrawListener {
            override fun onPreDraw(): Boolean {
                if (binding.puzzleContainer.width > 0 && binding.puzzleContainer.height > 0) {
                    loadOriginalImage()
                    splitImageIntoTiles()
                    shuffleTiles()
                    setupTileClickListeners()
                    binding.puzzleContainer.viewTreeObserver.removeOnPreDrawListener(this)
                }
                return true
            }
        })
    }

    private fun loadOriginalImage() {
        originalImage = BitmapFactory.decodeResource(resources, R.drawable.sample_image)
        val containerWidth = binding.puzzleContainer.width
        val containerHeight = binding.puzzleContainer.height
        originalImage = Bitmap.createScaledBitmap(
            originalImage,
            containerWidth,
            containerHeight,
            true
        )
    }

    private fun splitImageIntoTiles() {
        val tileWidth = originalImage.width / gridSize
        val tileHeight = originalImage.height / gridSize
        var tileIndex = 0

        for (row in 0 until gridSize) {
            for (col in 0 until gridSize) {
                val tileBitmap = Bitmap.createBitmap(
                    originalImage, col * tileWidth, row * tileHeight, tileWidth, tileHeight
                )
                val imageView = ImageView(requireContext())
                imageView.setImageBitmap(tileBitmap)
                imageView.layoutParams = LinearLayout.LayoutParams(tileWidth, tileHeight)
                imageView.tag = tileIndex++
                puzzleGrid[row][col] = imageView
                binding.puzzleContainer.addView(imageView)
            }
        }
    }

    private fun shuffleTiles() {
        val tiles = puzzleGrid.flatten().filterNotNull().shuffled().toMutableList()
        binding.puzzleContainer.removeAllViews()
        var index = 0
        for (row in 0 until gridSize) {
            for (col in 0 until gridSize) {
                puzzleGrid[row][col] = tiles[index++]
                binding.puzzleContainer.addView(puzzleGrid[row][col])
            }
        }
    }

    private fun setupTileClickListeners() {
        for (row in 0 until gridSize) {
            for (col in 0 until gridSize) {
                puzzleGrid[row][col]?.setOnClickListener { view ->
                    val currentPos = findTilePosition(view)
                    if (currentPos != null) {
                        handleTileClick(currentPos.first, currentPos.second)
                    }
                }
            }
        }
    }

    private fun findTilePosition(tile: View): Pair<Int, Int>? {
        for (row in 0 until gridSize) {
            for (col in 0 until gridSize) {
                if (puzzleGrid[row][col] == tile) {
                    return Pair(row, col)
                }
            }
        }
        return null
    }

    private fun handleTileClick(row: Int, col: Int) {
        val clickedTile = Pair(row, col)
        if (selectedTiles.contains(clickedTile)) {
            selectedTiles.remove(clickedTile)
            puzzleGrid[row][col]?.alpha = 1.0f // Reset visual feedback
        } else {
            selectedTiles.add(clickedTile)
            puzzleGrid[row][col]?.alpha = 0.5f // Visual feedback for selection

            if (selectedTiles.size == 2) {
                swapTiles()
            }
        }
    }

    private fun swapTiles() {
        if (selectedTiles.size != 2) return

        val (row1, col1) = selectedTiles[0]
        val (row2, col2) = selectedTiles[1]

        val temp = puzzleGrid[row1][col1]
        puzzleGrid[row1][col1] = puzzleGrid[row2][col2]
        puzzleGrid[row2][col2] = temp

        // Reset the visual feedback
        puzzleGrid[row1][col1]?.alpha = 1.0f
        puzzleGrid[row2][col2]?.alpha = 1.0f

        selectedTiles.clear()
        updateGridDisplay()
        checkIfSolved()
    }

    private fun updateGridDisplay() {
        binding.puzzleContainer.removeAllViews()
        for (row in 0 until gridSize) {
            for (col in 0 until gridSize) {
                puzzleGrid[row][col]?.let {
                    (it.parent as? ViewGroup)?.removeView(it)
                    binding.puzzleContainer.addView(it)
                }
            }
        }
    }

    private fun checkIfSolved() {
        var correct = true
        var tileIndex = 0
        for (row in 0 until gridSize) {
            for (col in 0 until gridSize) {
                if (puzzleGrid[row][col]?.tag as? Int != tileIndex++) {
                    correct = false
                    break
                }
            }
        }
        if (correct) {
            Toast.makeText(requireContext(), "Puzzle solved!", Toast.LENGTH_SHORT).show()
            solvedGames++
            Handler(Looper.getMainLooper()).postDelayed({
                loadNextLevel()
            }, 500)
        }
    }

    private fun loadNextLevel() {
        binding.roundInfo.text = "Round ${solvedGames + 1}"
        initializeGame()
    }

    override fun onTimerEnd() {
        AlertDialog.Builder(requireContext())
            .setTitle("Game Over")
            .setMessage("Score: $solvedGames")
            .setPositiveButton("OK") { _, _ -> activity?.finish() }
            .setOnDismissListener { activity?.finish() }
            .show()
    }
}
