package hu.bme.aut.fvf13l.retrobyteblitz.auth

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import hu.bme.aut.fvf13l.retrobyteblitz.MainActivity
import hu.bme.aut.fvf13l.retrobyteblitz.databinding.ActivityLoginBinding
import hu.bme.aut.fvf13l.retrobyteblitz.utility.SessionManager
import hu.bme.aut.fvf13l.retrobyteblitz.viewmodel.AuthViewModel
import hu.bme.aut.fvf13l.retrobyteblitz.viewmodel.AuthViewModelFactory
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private val userDao by lazy { UserDatabase.getDatabase(this).userDao() }
    private val authViewModel: AuthViewModel by viewModels {
        AuthViewModelFactory(userDao)
    }
    private lateinit var binding: ActivityLoginBinding
    private val registerActivityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            Toast.makeText(this, "Registration successful!", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Registration was canceled", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.loginButton.setOnClickListener {
            val email = binding.usernameEditText.text.toString()
            val password = binding.passwordEditText.text.toString()

            if (email.isNotBlank() && password.isNotBlank()) {
                authViewModel.login(email, password) { success, userId ->
                    if (success && userId != null) {
                        lifecycleScope.launch {
                            val username = userDao.getUserById(userId)?.username
                            if (username != null) {
                                SessionManager.saveUserSession(this@LoginActivity, username, userId)
                                startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                                finish()
                            } else {
                                Toast.makeText(this@LoginActivity, "Failed to fetch username", Toast.LENGTH_SHORT).show()
                            }
                        }
                    } else {
                        Toast.makeText(this,"Invalid credentials", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            }
        }

        binding.registerButton.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            registerActivityResultLauncher.launch(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        if (SessionManager.isLoggedIn(this)) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }
}