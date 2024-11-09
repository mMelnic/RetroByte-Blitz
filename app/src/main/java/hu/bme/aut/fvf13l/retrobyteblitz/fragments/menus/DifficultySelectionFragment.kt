package hu.bme.aut.fvf13l.retrobyteblitz.fragments.menus

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import hu.bme.aut.fvf13l.retrobyteblitz.R
import hu.bme.aut.fvf13l.retrobyteblitz.databinding.FragmentDifficultySelectionBinding
import hu.bme.aut.fvf13l.retrobyteblitz.fragments.games.SudokuGameFragment

class DifficultySelectionFragment : Fragment() {

    private var gameType: String? = null
    private var _binding: FragmentDifficultySelectionBinding? = null
    private val binding get() = _binding!!

    companion object {
        private const val ARG_GAME_TYPE = "gameType"

        fun newInstance(gameType: String): DifficultySelectionFragment {
            val fragment = DifficultySelectionFragment()
            val args = Bundle().apply { putString(ARG_GAME_TYPE, gameType) }
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        gameType = arguments?.getString(ARG_GAME_TYPE)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDifficultySelectionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonJunior.setOnClickListener { launchGameWithDifficulty("junior") }
        binding.buttonMaster.setOnClickListener { launchGameWithDifficulty("master") }
        binding.buttonExpert.setOnClickListener { launchGameWithDifficulty("expert") }
    }

    private fun launchGameWithDifficulty(difficulty: String) {
        val gameFragment = when (gameType) {
            "Sudoku" -> SudokuGameFragment.newInstance(difficulty)
            else -> null
        }
        gameFragment?.let {
            parentFragmentManager.beginTransaction()
                .replace(R.id.middleFragmentContainer, it)
                .addToBackStack(null)
                .commit()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
