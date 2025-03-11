package hu.bme.aut.fvf13l.retrobyteblitz.auth

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class UserRepository(private val userDao: UserDao) {

    fun registerUser(email: String, password: String, username: String) : Boolean{
        var isSuccess = false
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val firebaseUser = task.result?.user
                    if (firebaseUser != null) {
                        val userId = firebaseUser.uid
                        val user = User(userId = userId, username = username)

                        CoroutineScope(Dispatchers.IO).launch {
                            userDao.insertUser(user)
                        }
                        isSuccess = true
                    }
                } else {
                    Log.e("Firebase", "Registration failed: ${task.exception?.message}")
                    isSuccess = false
                }
            }
        return isSuccess
    }

    fun loginUser(email: String, password: String): Pair<Boolean, String?> {
        var result: Pair<Boolean, String?> = Pair(false, null)
        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val firebaseUser = task.result?.user
                    if (firebaseUser != null) {
                        val userId = firebaseUser.uid

                        CoroutineScope(Dispatchers.IO).launch {
                            val user = userDao.getUserById(userId)
                            result = if (user != null) {
                                Pair(true, user.userId)
                            } else {
                                Pair(false, null)
                            }
                        }
                    }
                } else {
                    result = Pair(false, null)
                }
            }
        return result
    }

    suspend fun updateUsername(oldUsername: String, newUsername: String): Boolean {
        if (newUsername.isEmpty()) {
            return false
        }

        val existingUser = userDao.getUserByUsername(newUsername)
        if (existingUser != null) {
            return false
        }
        userDao.updateUsername(oldUsername, newUsername)
        return true
    }
}