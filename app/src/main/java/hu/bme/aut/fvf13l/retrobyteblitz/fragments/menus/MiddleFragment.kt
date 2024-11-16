package hu.bme.aut.fvf13l.retrobyteblitz.fragments.menus

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import hu.bme.aut.fvf13l.retrobyteblitz.MainActivity
import hu.bme.aut.fvf13l.retrobyteblitz.databinding.FragmentMiddleBinding

class MiddleFragment : Fragment() {

    private var _binding: FragmentMiddleBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMiddleBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonDailyActivities.setOnClickListener {
            (activity as MainActivity).openCategory("Daily Exercises")
        }
        binding.buttonLogic.setOnClickListener {
            (activity as MainActivity).openCategory("Logic")
        }
        binding.buttonMemory.setOnClickListener {
            (activity as MainActivity).openCategory("Memory")
        }
        binding.buttonCalculation.setOnClickListener {
            (activity as MainActivity).openCategory("Calculation")
        }
        binding.buttonKnowledge.setOnClickListener {
            (activity as MainActivity).openCategory("Visual")
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}