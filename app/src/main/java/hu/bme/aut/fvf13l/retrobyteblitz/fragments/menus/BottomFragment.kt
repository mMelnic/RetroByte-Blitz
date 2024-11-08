package hu.bme.aut.fvf13l.retrobyteblitz.fragments.menus

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import hu.bme.aut.fvf13l.retrobyteblitz.MainActivity
import hu.bme.aut.fvf13l.retrobyteblitz.databinding.FragmentBottomBinding

class BottomFragment : Fragment() {

    private var _binding: FragmentBottomBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBottomBinding.inflate(inflater, container, false)
        binding.buttonNav1.setOnClickListener {
            val intent = Intent(activity, MainActivity::class.java)
            startActivity(intent)
        }
        return binding.root
    }

    // To implement onClick listeners for navigation buttons

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}