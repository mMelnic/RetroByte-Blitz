package hu.bme.aut.fvf13l.retrobyteblitz.fragments.menus

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import hu.bme.aut.fvf13l.retrobyteblitz.databinding.FragmentMiddleGamesBinding
import hu.bme.aut.fvf13l.retrobyteblitz.fragments.games.GameActivity

class MiddleGamesFragment : Fragment() {

    private var _binding: FragmentMiddleGamesBinding? = null
    private val binding get() = _binding!!

    private val categoryGames = mapOf(
        "Daily Exercises" to listOf("Game 1A", "Game 2A", "Game 3A"),
        "Logic" to listOf("Number Of", "Sudoku", "Slider"),
        "Memory" to listOf("Colors", "Grid", "Card"),
        "Calculation" to listOf("Calculation", "Sequence", "Moving Sum"),
        "Visual" to listOf("Descending", "Stroop", "Game 3E")
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
            binding.buttonGame1.text = games[0]
            binding.buttonGame2.text = games[1]
            binding.buttonGame3.text = games[2]
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
        when (gameName) {
            "Sudoku" -> {
                val intent = Intent(requireContext(), GameActivity::class.java).apply {
                    putExtra("GAME_NAME", "Sudoku")
                    putExtra("TIME_LIMIT", 60000L)
                }
                startActivity(intent)
            }
            "Calculation" -> {
                val intent = Intent(requireContext(), GameActivity::class.java).apply {
                    putExtra("GAME_NAME", "Calculation")
                    putExtra("TIME_LIMIT", 120000L)
                }
                startActivity(intent)
            }
            "Colors" -> {
                val intent = Intent(requireContext(), GameActivity::class.java).apply {
                    putExtra("GAME_NAME", "Colors")
                    putExtra("TIME_LIMIT", 60000L)
                }
                startActivity(intent)
            }
            "Grid" -> {
                val intent = Intent(requireContext(), GameActivity::class.java).apply {
                    putExtra("GAME_NAME", "Grid")
                    putExtra("TIME_LIMIT", 60000L)
                }
                startActivity(intent)
            }
            "Sequence" -> {
                val intent = Intent(requireContext(), GameActivity::class.java).apply {
                    putExtra("GAME_NAME", "Sequence")
                    putExtra("TIME_LIMIT", 60000L)
                }
                startActivity(intent)
            }
            "Number Of" -> {
                val intent = Intent(requireContext(), GameActivity::class.java).apply {
                    putExtra("GAME_NAME", "Number Of")
                    putExtra("TIME_LIMIT", 60000L)
                }
                startActivity(intent)
            }
            "Moving Sum" -> {
                val intent = Intent(requireContext(), GameActivity::class.java).apply {
                    putExtra("GAME_NAME", "Moving Sum")
                    putExtra("TIME_LIMIT", 60000L)
                }
                startActivity(intent)
            }
            "Descending" -> {
                val intent = Intent(requireContext(), GameActivity::class.java).apply {
                    putExtra("GAME_NAME", "Descending")
                    putExtra("TIME_LIMIT", 60000L)
                }
                startActivity(intent)
            }
            "Card" -> {
                val intent = Intent(requireContext(), GameActivity::class.java).apply {
                    putExtra("GAME_NAME", "Card")
                    putExtra("TIME_LIMIT", 120000L)
                }
                startActivity(intent)
            }
            "Slider" -> {
                val intent = Intent(requireContext(), GameActivity::class.java).apply {
                    putExtra("GAME_NAME", "Slider")
                    putExtra("TIME_LIMIT", 60000L)
                }
                startActivity(intent)
            }
            "Stroop" -> {
                val intent = Intent(requireContext(), GameActivity::class.java).apply {
                    putExtra("GAME_NAME", "Stroop")
                    putExtra("TIME_LIMIT", 60000L)
                }
                startActivity(intent)
            }
            // ToDo add more cases here for other games
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}