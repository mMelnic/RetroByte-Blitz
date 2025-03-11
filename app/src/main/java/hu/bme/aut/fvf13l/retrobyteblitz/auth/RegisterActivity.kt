package hu.bme.aut.fvf13l.retrobyteblitz.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import hu.bme.aut.fvf13l.retrobyteblitz.MainActivity
import hu.bme.aut.fvf13l.retrobyteblitz.databinding.ActivityRegisterBinding
import hu.bme.aut.fvf13l.retrobyteblitz.viewmodel.AuthViewModel
import hu.bme.aut.fvf13l.retrobyteblitz.viewmodel.AuthViewModelFactory

class RegisterActivity : AppCompatActivity() {

    private val userDao by lazy { UserDatabase.getDatabase(this).userDao() }

    private val authViewModel: AuthViewModel by viewModels {
        AuthViewModelFactory(userDao)
    }
    private lateinit var binding: ActivityRegisterBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.registerButton.setOnClickListener {
            val username = binding.usernameEditText.text.toString()
            val password = binding.passwordEditText.text.toString()
            val email = binding.emailEditText.text.toString()

            when {
                username.isBlank() || password.isBlank() || email.isBlank() -> {
                    Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                }
                username.contains(" ") -> {
                    Toast.makeText(this, "Username cannot contain spaces", Toast.LENGTH_SHORT).show()
                }
                password.contains(" ") -> {
                    Toast.makeText(this, "Password cannot contain spaces", Toast.LENGTH_SHORT).show()
                }
                else -> {
                    authViewModel.register(email, username, password) { success ->
                        if (success) {
                            startActivity(Intent(this, LoginActivity::class.java))
                            finish()
                        } else {
                            Toast.makeText(this, "Username already exists", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }
    }
}