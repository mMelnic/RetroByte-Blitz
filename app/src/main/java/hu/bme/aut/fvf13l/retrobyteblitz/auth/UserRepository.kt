package hu.bme.aut.fvf13l.retrobyteblitz.auth

import hu.bme.aut.fvf13l.retrobyteblitz.utility.SecurityUtil

class UserRepository(private val userDao: UserDao) {

    suspend fun registerUser(username: String, password: String): Boolean {
        val existingUser = userDao.getUserByUsername(username)
        if (existingUser != null) {
            return false
        }
        val hashedPassword = SecurityUtil.hashPassword(password)
        val user = User(username = username, hashedPassword = hashedPassword)
        userDao.insertUser(user)
        return true
    }

    suspend fun loginUser(username: String, password: String): Boolean {
        val user = userDao.getUserByUsername(username) ?: return false
        return SecurityUtil.verifyPassword(password, user.hashedPassword)
    }
}