package hu.bme.aut.fvf13l.retrobyteblitz.auth

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey val userId: String,
    val username: String,
)