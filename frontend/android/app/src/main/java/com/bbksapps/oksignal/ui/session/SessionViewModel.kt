package com.bbksapps.oksignal.ui.session

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bbksapps.oksignal.data.local.model.AppMode
import com.bbksapps.oksignal.data.local.repository.AppSessionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class LoginUiState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val selectedMode: AppMode? = null,
    val errorMessage: String? = null
)

class SessionViewModel(
    private val appSessionRepository: AppSessionRepository
) : ViewModel() {

    private val _loginUiState = MutableStateFlow(LoginUiState())
    val loginUiState: StateFlow<LoginUiState> = _loginUiState.asStateFlow()

    fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _loginUiState.value = LoginUiState(
                errorMessage = "Email and password are required."
            )
            return
        }

        viewModelScope.launch {
            _loginUiState.value = LoginUiState(isLoading = true)

            try {
                // TODO: replace with real backend login API
                val mockCanActAsGuardian = true
                val mockCanActAsMember = true

                appSessionRepository.onLoginSuccess(
                    userId = "mock-user-123",
                    email = email,
                    displayName = "Brandon",
                    planType = "free",
                    accessToken = "mock-access-token",
                    refreshToken = null,
                    tokenExpiresAt = null,
                    canActAsGuardian = mockCanActAsGuardian,
                    canActAsMember = mockCanActAsMember
                )

                val mode = when {
                    mockCanActAsGuardian -> AppMode.GUARDIAN
                    mockCanActAsMember -> AppMode.MEMBER
                    else -> AppMode.MEMBER
                }

                _loginUiState.value = LoginUiState(
                    isLoading = false,
                    isSuccess = true,
                    selectedMode = mode
                )
            } catch (e: Exception) {
                _loginUiState.value = LoginUiState(
                    isLoading = false,
                    errorMessage = e.message ?: "Login failed."
                )
            }
        }
    }

    fun consumeSuccess() {
        _loginUiState.value = _loginUiState.value.copy(
            isSuccess = false,
            selectedMode = null
        )
    }

    fun clearError() {
        _loginUiState.value = _loginUiState.value.copy(
            errorMessage = null
        )
    }

    fun logout() {
        viewModelScope.launch {
            appSessionRepository.logout()
        }
    }
}