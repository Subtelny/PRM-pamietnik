package pl.sjanda.jpamietnik.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pl.sjanda.jpamietnik.data.PasswordManager

data class LockScreenState(
    val isPasswordSet: Boolean = false,
    val isLoading: Boolean = true,
    val error: String? = null,
    val unlocked: Boolean = false,
    val needsToSetPasswordFirst: Boolean = false
)

class LockScreenViewModel(application: Application) : AndroidViewModel(application) {

    private val passwordManager = PasswordManager(application.applicationContext)

    private val _uiState = MutableStateFlow(LockScreenState())
    val uiState: StateFlow<LockScreenState> = _uiState.asStateFlow()

    init {
        checkPasswordStatus()
    }

    private fun checkPasswordStatus() {
        viewModelScope.launch {
            val isSet = passwordManager.isPasswordSet()
            _uiState.update {
                it.copy(
                    isPasswordSet = isSet,
                    isLoading = false,
                    needsToSetPasswordFirst = !isSet
                )
            }
        }
    }

    fun onPasswordEnter(password: String) {
        if (_uiState.value.needsToSetPasswordFirst) {
            if (password.length < 4) {
                _uiState.update { it.copy(error = "Hasło musi mieć co najmniej 4 znaki.") }
                return
            }
            passwordManager.savePassword(password)
            _uiState.update {
                it.copy(
                    unlocked = true,
                    error = null,
                    needsToSetPasswordFirst = false,
                    isPasswordSet = true
                )
            }
        } else {
            val isCorrect = passwordManager.verifyPassword(password)
            if (isCorrect) {
                _uiState.update { it.copy(unlocked = true, error = null) }
            } else {
                _uiState.update { it.copy(error = "Nieprawidłowe hasło.") }
            }
        }
    }

    fun consumeUnlockEvent() {
        _uiState.update { it.copy(unlocked = false) }
    }

    fun consumeError() {
        _uiState.update { it.copy(error = null) }
    }
}