package hu.bme.aut.fvf13l.retrobyteblitz.auth

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface UserDao {
    @Insert
    suspend fun insertUser(user: User)

    @Query("SELECT * FROM users WHERE username = :username LIMIT 1")
    suspend fun getUserByUsername(username: String): User?

    @Query("UPDATE users SET username = :newUsername WHERE username = :oldUsername")
    suspend fun updateUsername(oldUsername: String, newUsername: String)
}