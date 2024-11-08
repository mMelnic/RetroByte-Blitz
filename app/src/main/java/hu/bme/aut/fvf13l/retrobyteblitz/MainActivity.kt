package hu.bme.aut.fvf13l.retrobyteblitz

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import hu.bme.aut.fvf13l.retrobyteblitz.databinding.ActivityMainBinding
import hu.bme.aut.fvf13l.retrobyteblitz.fragments.menus.BottomFragment
import hu.bme.aut.fvf13l.retrobyteblitz.fragments.menus.MiddleFragment
import hu.bme.aut.fvf13l.retrobyteblitz.fragments.menus.TopCategoryFragment
import hu.bme.aut.fvf13l.retrobyteblitz.fragments.menus.MiddleGamesFragment
import hu.bme.aut.fvf13l.retrobyteblitz.fragments.menus.TopFragment

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportFragmentManager.beginTransaction()
            .replace(R.id.topFragmentContainer, TopFragment())
            .replace(R.id.middleFragmentContainer, MiddleFragment())
            .replace(R.id.bottomFragmentContainer, BottomFragment())
            .commit()
    }

    // Method to open the category view
    fun openCategory(category: String) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.topFragmentContainer, TopCategoryFragment.newInstance(category))
            .replace(R.id.middleFragmentContainer, MiddleGamesFragment())
            .addToBackStack(null)
            .commit()
    }
}