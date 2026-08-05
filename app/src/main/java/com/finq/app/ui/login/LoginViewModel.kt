package com.finq.app.ui.login

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.finq.app.data.repository.AuthRepository
import com.finq.app.ui.userErrorMessage
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LoginUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
)

sealed interface LoginEvent {
    data object LoginSuccess : LoginEvent
}

class LoginViewModel(
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    private val _events = Channel<LoginEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    // ── 카카오 로그인 ─────────────────────────────────────────────────────────

    fun loginWithKakao(context: Context) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            authRepository.loginWithKakao(context)
                .onSuccess {
                    _uiState.update { it.copy(isLoading = false) }
                    _events.send(LoginEvent.LoginSuccess)
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isLoading = false, error = userErrorMessage(e, "카카오 로그인 실패")) }
                }
        }
    }

    // ── 구글 로그인 ───────────────────────────────────────────────────────────

    fun loginWithGoogle(context: Context) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            authRepository.loginWithGoogle(context)
                .onSuccess {
                    _uiState.update { it.copy(isLoading = false) }
                    _events.send(LoginEvent.LoginSuccess)
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isLoading = false, error = userErrorMessage(e, "구글 로그인 실패")) }
                }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    // ── Factory ───────────────────────────────────────────────────────────────

    companion object {
        fun factory(authRepository: AuthRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                    LoginViewModel(authRepository) as T
            }
    }
}
