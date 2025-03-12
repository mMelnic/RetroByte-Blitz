package hu.bme.aut.fvf13l.retrobyteblitz.auth

import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class UserRepository(private val userDao: UserDao) {

    suspend fun registerUser(email: String, password: String, username: String): Boolean {
        return try {
            val authResult = FirebaseAuth.getInstance()
                .createUserWithEmailAndPassword(email, password)
                .await()

            val firebaseUser = authResult.user
            if (firebaseUser != null) {
                val userId = firebaseUser.uid

                withContext(Dispatchers.IO) {
                    val user = User(userId = userId, username = username)
                    userDao.insertUser(user)
                }

                true
            } else {
                false
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun loginUser(email: String, password: String): Pair<Boolean, String?> {
        return try {
            val authResult = FirebaseAuth.getInstance()
                .signInWithEmailAndPassword(email, password)
                .await()

            val firebaseUser = authResult.user
            if (firebaseUser != null) {
                val userId = firebaseUser.uid

                val user = withContext(Dispatchers.IO) { userDao.getUserById(userId) }

                if (user != null) {
                    Pair(true, user.userId)
                } else {
                    val defaultUsername = "User${userId.take(3)}"
                    val newUser = User(userId = userId, username = defaultUsername)
                    withContext(Dispatchers.IO) {
                        userDao.insertUser(newUser)
                    }
                    Pair(true, newUser.userId)
                }
            } else {
                Pair(false, null)
            }
        } catch (e: Exception) {
            Pair(false, null)
        }
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