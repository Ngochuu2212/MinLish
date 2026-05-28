package com.example.english_app.ui.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.english_app.data.repository.AuthRepository
import com.example.english_app.data.repository.AuthResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AuthUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val success: Boolean = false,
    /** Sau khi đăng ký hoặc resend — email xác minh / reset đã được gửi */
    val verificationSent: Boolean = false,
    /** Login thất bại vì email chưa được xác minh */
    val emailNotVerified: Boolean = false
)

/**
 * Trạng thái Forgot Password — Firebase gửi reset link qua email, 1 bước.
 */
data class ForgotPasswordState(
    val isLoading: Boolean = false,
    val emailSent: Boolean = false,
    val error: String? = null
)

class AuthViewModel(private val authRepository: AuthRepository) : ViewModel() {

    val currentUserId = authRepository.currentUserId

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    private val _forgotState = MutableStateFlow(ForgotPasswordState())
    val forgotState: StateFlow<ForgotPasswordState> = _forgotState.asStateFlow()

    // ── Login ────────────────────────────────────────────────────────────────
    fun login(email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState(isLoading = true)
            when (val result = authRepository.login(email, password)) {
                is AuthResult.Success       -> _uiState.value = AuthUiState(success = true)
                is AuthResult.Error         -> _uiState.value = AuthUiState(error = result.message)
                is AuthResult.EmailNotVerified ->
                    _uiState.value = AuthUiState(emailNotVerified = true,
                        error = "Your email is not verified yet. Please check your inbox.")
                else -> _uiState.value = AuthUiState(error = "Unexpected error")
            }
        }
    }

    // ── Register ─────────────────────────────────────────────────────────────
    fun register(name: String, email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState(isLoading = true)
            when (val result = authRepository.register(name, email, password)) {
                is AuthResult.VerificationEmailSent ->
                    _uiState.value = AuthUiState(verificationSent = true)
                is AuthResult.Error ->
                    _uiState.value = AuthUiState(error = result.message)
                else -> _uiState.value = AuthUiState(error = "Unexpected error")
            }
        }
    }

    // ── Resend verification email ─────────────────────────────────────────────
    fun resendVerificationEmail(email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            when (val result = authRepository.resendVerificationEmail(email, password)) {
                is AuthResult.VerificationEmailSent ->
                    _uiState.value = AuthUiState(verificationSent = true)
                is AuthResult.Error ->
                    _uiState.value = _uiState.value.copy(isLoading = false, error = result.message)
                else -> _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }

    // ── Logout ───────────────────────────────────────────────────────────────
    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
            _uiState.value = AuthUiState()
        }
    }

    // ── Google Sign-In ───────────────────────────────────────────────────────
    fun loginWithGoogle(idToken: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState(isLoading = true)
            when (val result = authRepository.loginWithGoogle(idToken)) {
                is AuthResult.Success -> _uiState.value = AuthUiState(success = true)
                is AuthResult.Error   -> _uiState.value = AuthUiState(error = result.message)
                else -> _uiState.value = AuthUiState(error = "Unexpected error")
            }
        }
    }

    fun clearError()                 { _uiState.value = _uiState.value.copy(error = null) }
    fun setGoogleError(msg: String)  { _uiState.value = AuthUiState(error = msg) }
    fun clearSuccess()               { _uiState.value = AuthUiState() }

    // ── Forgot Password — Firebase gửi reset link 1 bước ─────────────────────
    fun forgotSendResetEmail(email: String) {
        viewModelScope.launch {
            _forgotState.value = ForgotPasswordState(isLoading = true)
            when (val result = authRepository.sendPasswordResetEmail(email)) {
                is AuthResult.VerificationEmailSent ->
                    _forgotState.value = ForgotPasswordState(emailSent = true)
                is AuthResult.Error ->
                    _forgotState.value = ForgotPasswordState(error = result.message)
                else -> _forgotState.value = ForgotPasswordState(error = "Unexpected error")
            }
        }
    }

    fun resetForgotState() { _forgotState.value = ForgotPasswordState() }
}

class AuthViewModelFactory(private val repo: AuthRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return AuthViewModel(repo) as T
    }
}
