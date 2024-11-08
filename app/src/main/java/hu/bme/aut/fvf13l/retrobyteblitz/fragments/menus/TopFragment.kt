package hu.bme.aut.fvf13l.retrobyteblitz.fragments.menus

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import hu.bme.aut.fvf13l.retrobyteblitz.databinding.FragmentTopBinding

class TopFragment : Fragment() {

    private var _binding: FragmentTopBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTopBinding.inflate(inflater, container, false)
        return binding.root
    }

    fun setCategoryTitle(title: String) {
        binding.topTextView.text = title
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}