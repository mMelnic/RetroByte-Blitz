package hu.bme.aut.fvf13l.retrobyteblitz

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.lifecycle.lifecycleScope
import hu.bme.aut.fvf13l.retrobyteblitz.auth.LoginActivity
import hu.bme.aut.fvf13l.retrobyteblitz.auth.UserDatabase
import hu.bme.aut.fvf13l.retrobyteblitz.auth.UserRepository
import hu.bme.aut.fvf13l.retrobyteblitz.databinding.ActivityMainBinding
import hu.bme.aut.fvf13l.retrobyteblitz.fragments.menus.BottomFragment
import hu.bme.aut.fvf13l.retrobyteblitz.fragments.menus.MiddleFragment
import hu.bme.aut.fvf13l.retrobyteblitz.fragments.menus.TopCategoryFragment
import hu.bme.aut.fvf13l.retrobyteblitz.fragments.menus.MiddleGamesFragment
import hu.bme.aut.fvf13l.retrobyteblitz.fragments.menus.TopFragment
import hu.bme.aut.fvf13l.retrobyteblitz.utility.SessionManager
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var userRepository: UserRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportFragmentManager.beginTransaction()
            .replace(R.id.topFragmentContainer, TopFragment())
            .replace(R.id.middleFragmentContainer, MiddleFragment())
            .replace(R.id.bottomFragmentContainer, BottomFragment())
            .commit()

        val userDao = UserDatabase.getDatabase(this).userDao()
        userRepository = UserRepository(userDao)

        setupDrawer()
    }

    fun openCategory(category: String) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.topFragmentContainer, TopCategoryFragment.newInstance(category))
            .replace(R.id.middleFragmentContainer, MiddleGamesFragment.newInstance(category))
            .addToBackStack("GAME_AND_TIMER_STACK")
            .commit()
    }

    fun openNavigationDrawer() {
        binding.drawerLayout.openDrawer(GravityCompat.END)
    }

    private fun setupDrawer() {
        val profilePicture = binding.profilePicture
        val usernameField = binding.usernameField
        val editIcon = binding.editUsernameIcon
        val logoutButton = binding.logoutButton

        val username = SessionManager.getUsername(this)
        usernameField.setText(username)

        profilePicture.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, REQUEST_IMAGE_PICK)
        }

        handleUsernameUpdate(usernameField, editIcon)

        logoutButton.setOnClickListener {
            showLogoutDialog()
        }

        populateInstructions()
    }

    private fun handleUsernameUpdate(usernameField: EditText, editIcon: ImageView) {
        editIcon.setOnClickListener {
            usernameField.isEnabled = true
            usernameField.requestFocus()

            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(usernameField, InputMethodManager.SHOW_IMPLICIT)
        }

        usernameField.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                val newUsername = usernameField.text.toString().trim()
                val currentUsername = SessionManager.getUsername(this)

                if (newUsername.isNotEmpty() && newUsername != currentUsername) {
                    lifecycleScope.launch {
                        val isUpdated = userRepository.updateUsername(currentUsername!!, newUsername)
                        if (isUpdated) {
                            SessionManager.updateUsername(this@MainActivity, newUsername)
                            Toast.makeText(this@MainActivity, "Username updated successfully", Toast.LENGTH_SHORT).show()
                        } else {
                            usernameField.setText(currentUsername)
                            Toast.makeText(this@MainActivity, "Username already exists", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else if (newUsername.isEmpty()) {
                    usernameField.setText(currentUsername)
                    Toast.makeText(this, "Username cannot be empty", Toast.LENGTH_SHORT).show()
                }

                usernameField.isEnabled = false
            }
        }

        usernameField.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                usernameField.clearFocus() // Triggers the onFocusChangeListener
                true
            } else {
                false
            }
        }
    }


    private fun populateInstructions() {
        val instructionsContainer = binding.instructionsContainer

        val categories = mapOf(
            getString(R.string.category1_name) to listOf(
                getString(R.string.sudoku_instructions),
                getString(R.string.slider_instructions),
                getString(R.string.number_of_instructions)
            ),
            getString(R.string.category2_name) to listOf(
                getString(R.string.colors_instructions),
                getString(R.string.grid_instructions),
                getString(R.string.card_instructions)
            ),
            getString(R.string.category3_name) to listOf(
                getString(R.string.calculation_instructions),
                getString(R.string.sequence_instructions),
                getString(R.string.moving_sum_instructions)
            ),
            getString(R.string.category4_name) to listOf(
                getString(R.string.descending_instructions),
                getString(R.string.stroop_instructions),
                getString(R.string.roman_num_instructions)
            )
        )

        categories.forEach { (categoryName, instructions) ->
            val categoryTextView = TextView(this).apply {
                text = categoryName
                textSize = 16f
                setTypeface(null, Typeface.BOLD)
                setPadding(0, 16, 0, 8)
            }
            instructionsContainer.addView(categoryTextView)

            instructions.forEach { instruction ->
                val instructionTextView = TextView(this).apply {
                    text = "- $instruction"
                    textSize = 14f
                    setPadding(16, 4, 0, 4)
                }
                instructionsContainer.addView(instructionTextView)
            }
        }
    }


    private fun showLogoutDialog() {
        AlertDialog.Builder(this)
            .setMessage("Are you sure you want to log out?")
            .setPositiveButton("Yes") { _, _ ->
                SessionManager.clearSession(this)
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            }
            .setNegativeButton("No", null)
            .show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_PICK && resultCode == Activity.RESULT_OK) {
            val uri: Uri? = data?.data
            val profilePicture = binding.profilePicture
            profilePicture.setImageURI(uri)
        }
    }

    companion object {
        private const val REQUEST_IMAGE_PICK = 1
    }
}