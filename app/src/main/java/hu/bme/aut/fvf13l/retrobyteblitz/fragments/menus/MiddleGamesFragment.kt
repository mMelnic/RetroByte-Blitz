package hu.bme.aut.fvf13l.retrobyteblitz.fragments.menus

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import hu.bme.aut.fvf13l.retrobyteblitz.databinding.FragmentMiddleGamesBinding
import hu.bme.aut.fvf13l.retrobyteblitz.model.GameActivity

class MiddleGamesFragment : Fragment() {

    private var _binding: FragmentMiddleGamesBinding? = null
    private val binding get() = _binding!!

    private val categoryGames = mapOf(
        "Logic" to listOf("Number Of", "Sudoku", "Slider"),
        "Memory" to listOf("Colors", "Grid", "Card"),
        "Calculation" to listOf("Calculation", "Sequence", "Moving Sum"),
        "Visual" to listOf("Descending", "Stroop", "Roman Numerals")
    )

    private var categoryName: String? = null

    companion object {
        private const val ARG_CATEGORY_NAME = "category_name"

        fun newInstance(categoryName: String): MiddleGamesFragment {
            val fragment = MiddleGamesFragment()
            val args = Bundle()
            args.putString(ARG_CATEGORY_NAME, categoryName)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentMiddleGamesBinding.inflate(inflater, container, false)
        arguments?.getString(ARG_CATEGORY_NAME)?.let {
            categoryName = it
        }

        val games = categoryGames[categoryName] ?: emptyList()

        if (games.size == 3) {
            binding.textViewGame1.text = games[0]
            binding.textViewGame2.text = games[1]
            binding.textViewGame3.text = games[2]
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val games = categoryGames[categoryName] ?: emptyList()

        binding.buttonGame1.setOnClickListener {
            launchGame(games[0])
        }

        binding.buttonGame2.setOnClickListener {
            launchGame(games[1])
        }

        binding.buttonGame3.setOnClickListener {
            launchGame(games[2])
        }
    }

    private fun launchGame(gameName: String) {
        val gameTimeLimits = mapOf(
            "Sudoku" to 600000L,
            "Calculation" to 120000L,
            "Colors" to 120000L,
            "Grid" to 60000L,
            "Sequence" to 120000L,
            "Number Of" to 60000L,
            "Moving Sum" to 60000L,
            "Descending" to 60000L,
            "Card" to 120000L,
            "Slider" to 60000L,
            "Stroop" to 60000L,
            "Roman Numerals" to 60000L
        )

        gameTimeLimits[gameName]?.let { timeLimit ->
            val intent = Intent(requireContext(), GameActivity::class.java).apply {
                putExtra("GAME_NAME", gameName)
                putExtra("TIME_LIMIT", timeLimit)
            }
            startActivity(intent)
        } ?: run {
            Log.e("GameLauncher", "Unknown game name: $gameName")
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}