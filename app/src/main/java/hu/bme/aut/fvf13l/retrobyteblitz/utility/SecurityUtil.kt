package hu.bme.aut.fvf13l.retrobyteblitz.utility

import org.mindrot.jbcrypt.BCrypt

object SecurityUtil {

    fun hashPassword(password: String): String {
        return BCrypt.hashpw(password, BCrypt.gensalt())
    }

    fun verifyPassword(inputPassword: String, storedHash: String): Boolean {
        return BCrypt.checkpw(inputPassword, storedHash)
    }
}