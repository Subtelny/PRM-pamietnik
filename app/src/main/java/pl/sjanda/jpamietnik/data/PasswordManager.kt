package pl.sjanda.jpamietnik.data

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

class PasswordManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("password_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_PASSWORD = "password"
    }

    fun isPasswordSet(): Boolean {
        return prefs.contains(KEY_PASSWORD)
    }

    fun savePassword(password: String) {
        prefs.edit {
            putString(KEY_PASSWORD, password)
        }
    }

    fun verifyPassword(password: String): Boolean {
        if (!isPasswordSet()) {
            return false
        }

        val storedPassword = prefs.getString(KEY_PASSWORD, null)
        if (storedPassword == null) {
            return false
        }
        return password == storedPassword
    }

}