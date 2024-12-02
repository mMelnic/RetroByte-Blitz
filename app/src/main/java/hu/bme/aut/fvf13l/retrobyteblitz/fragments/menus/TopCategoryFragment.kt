package hu.bme.aut.fvf13l.retrobyteblitz.fragments.menus

import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import hu.bme.aut.fvf13l.retrobyteblitz.R
import hu.bme.aut.fvf13l.retrobyteblitz.databinding.FragmentTopCategoryBinding

class TopCategoryFragment : Fragment() {

    private var _binding: FragmentTopCategoryBinding? = null
    private val binding get() = _binding!!

    companion object {
        private const val ARG_CATEGORY_TITLE = "category_title"

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

        binding.categoryTitleTextView.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                binding.categoryTitleTextView.viewTreeObserver.removeOnGlobalLayoutListener(this)

                val padding = 16
                val width = binding.categoryTitleTextView.width
                val height = binding.categoryTitleTextView.height

                val drawable = ContextCompat.getDrawable(requireContext(), R.drawable.blue_button)
                val bitmap = (drawable as BitmapDrawable).bitmap

                val paddedWidth = width - padding * 2
                val paddedHeight = height - padding * 2

                val scaledBitmap = Bitmap.createScaledBitmap(bitmap, paddedWidth, paddedHeight, true)
                val scaledDrawable = BitmapDrawable(resources, scaledBitmap)

                binding.categoryTitleTextView.background = scaledDrawable

                binding.categoryTitleTextView.setPadding(padding, padding, padding, padding)
                binding.categoryTitleTextView.gravity = Gravity.CENTER
            }
        })


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