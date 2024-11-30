package hu.bme.aut.fvf13l.retrobyteblitz.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import hu.bme.aut.fvf13l.retrobyteblitz.auth.UserRepository
import kotlinx.coroutines.launch

class AuthViewModel(private val repository: UserRepository) : ViewModel() {

    fun register(username: String, password: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val success = repository.registerUser(username, password)
            onResult(success)
        }
    }

    fun login(username: String, password: String, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            val (success, userId) = repository.loginUser(username, password)
            onResult(success, userId)
        }
    }
}