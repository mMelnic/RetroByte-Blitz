package hu.bme.aut.fvf13l.retrobyteblitz.auth

import hu.bme.aut.fvf13l.retrobyteblitz.utility.SecurityUtil
import java.util.UUID

class UserRepository(private val userDao: UserDao) {

    suspend fun registerUser(username: String, password: String): Boolean {
        val existingUser = userDao.getUserByUsername(username)
        if (existingUser != null) {
            return false
        }
        val hashedPassword = SecurityUtil.hashPassword(password)
        val userId = UUID.randomUUID().toString()
        val user = User(username = username, userId = userId, hashedPassword = hashedPassword)
        userDao.insertUser(user)
        return true
    }

    suspend fun loginUser(username: String, password: String): Pair<Boolean, String?> {
        val user = userDao.getUserByUsername(username) ?: return Pair(false, null)
        return if (SecurityUtil.verifyPassword(password, user.hashedPassword)) {
            Pair(true, user.userId)
        } else {
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