package hu.bme.aut.fvf13l.retrobyteblitz.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import hu.bme.aut.fvf13l.retrobyteblitz.MainActivity
import hu.bme.aut.fvf13l.retrobyteblitz.databinding.ActivityLoginBinding
import hu.bme.aut.fvf13l.retrobyteblitz.utility.SessionManager
import hu.bme.aut.fvf13l.retrobyteblitz.viewmodel.AuthViewModel
import hu.bme.aut.fvf13l.retrobyteblitz.viewmodel.AuthViewModelFactory

class LoginActivity : AppCompatActivity() {

    private val userDao by lazy { UserDatabase.getDatabase(this).userDao() }
    private val authViewModel: AuthViewModel by viewModels {
        AuthViewModelFactory(userDao)
    }
    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.loginButton.setOnClickListener {
            val username = binding.usernameEditText.text.toString()
            val password = binding.passwordEditText.text.toString()

            if (username.isNotBlank() && password.isNotBlank()) {
                authViewModel.login(username, password) { success ->
                    if (success) {
                        // Save session and navigate to the main screen on successful login
                        SessionManager.saveUserSession(this, username)
                        startActivity(Intent(this, MainActivity::class.java))
                        finish()
                    } else {
                        // Show error message if login fails
                        Toast.makeText(this, "Invalid credentials", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Automatically navigate to main screen if user is already logged in
        if (SessionManager.isLoggedIn(this)) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }
}