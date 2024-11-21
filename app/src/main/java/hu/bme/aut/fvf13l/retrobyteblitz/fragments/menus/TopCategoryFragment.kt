package hu.bme.aut.fvf13l.retrobyteblitz.fragments.menus

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import hu.bme.aut.fvf13l.retrobyteblitz.databinding.FragmentTopCategoryBinding

class TopCategoryFragment : Fragment() {

    private var _binding: FragmentTopCategoryBinding? = null
    private val binding get() = _binding!!

    companion object {
        private const val ARG_CATEGORY_TITLE = "category_title"

        // newInstance to pass arguments to the fragment
        fun newInstance(categoryTitle: String): TopCategoryFragment {
            val fragment = TopCategoryFragment()
            val args = Bundle()
            args.putString(ARG_CATEGORY_TITLE, categoryTitle)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTopCategoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val categoryTitle = arguments?.getString(ARG_CATEGORY_TITLE) ?: "Category"

        binding.categoryTitleTextView.text = categoryTitle

        binding.backButton.setOnClickListener {
            val parentActivity = activity

            if (parentActivity != null && parentFragmentManager.backStackEntryCount > 0) {
                parentFragmentManager.popBackStack()
            } else {
                parentActivity?.onBackPressed()
            }
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}