package hu.bme.aut.fvf13l.retrobyteblitz.auth

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey val username: String,
    val userId: String,
    val hashedPassword: String
)