package hu.bme.aut.fvf13l.retrobyteblitz.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import hu.bme.aut.fvf13l.retrobyteblitz.auth.UserDao
import hu.bme.aut.fvf13l.retrobyteblitz.auth.UserRepository

class AuthViewModelFactory(private val userDao: UserDao) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
            // Initialize UserRepository with UserDao and pass to AuthViewModel
            val userRepository = UserRepository(userDao)
            @Suppress("UNCHECKED_CAST")
            return AuthViewModel(userRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}