package com.bbksapps.oksignal.ui.session

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bbksapps.oksignal.data.local.model.AppMode
import com.bbksapps.oksignal.data.local.repository.AppSessionRepository
import com.bbksapps.oksignal.data.local.repository.AuthRepository
import com.bbksapps.oksignal.ui.common.AppDefaults
import com.bbksapps.oksignal.ui.common.UiMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.first

data class LoginUiState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val selectedMode: AppMode? = null,
    val errorMessage: UiMessage? = null
)

class SessionViewModel(
    private val appSessionRepository: AppSessionRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _loginUiState = MutableStateFlow(LoginUiState())
    val loginUiState: StateFlow<LoginUiState> = _loginUiState.asStateFlow()

    fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _loginUiState.value = LoginUiState(
                errorMessage = UiMessage.LOGIN_REQUIRED_FIELDS
            )
            return
        }

        viewModelScope.launch {
            _loginUiState.value = LoginUiState(isLoading = true)

            try {
                val appSession = appSessionRepository.appSessionState.first()
                val deviceId = appSession.device.deviceId

                if (deviceId.isNullOrBlank()) {
                    _loginUiState.value = LoginUiState(
                        isLoading = false,
                        errorMessage = UiMessage.DEVICE_ID_NOT_READY
                    )
                    return@launch
                }

                val response = authRepository.login(
                    email = email.trim(),
                    password = password,
                    deviceId = deviceId,
                    deviceName = android.os.Build.MODEL ?: AppDefaults.DEFAULT_DEVICE_NAME
                )

                if (!response.success || response.user == null || response.token.isNullOrBlank()) {
                    _loginUiState.value = LoginUiState(
                        isLoading = false,
                        errorMessage = UiMessage.LOGIN_FAILED
                    )
                    return@launch
                }

                appSessionRepository.onLoginSuccess(
                    userId = response.user.id,
                    email = response.user.email ?: email.trim(),
                    displayName = response.user.display_name ?: "OKSignal User",
                    planType = response.user.plan_type ?: "free",
                    accessToken = response.token,
                    refreshToken = null,
                    tokenExpiresAt = null,
                    canActAsGuardian = true,
                    canActAsMember = true
                )

                _loginUiState.value = LoginUiState(
                    isLoading = false,
                    isSuccess = true,
                    selectedMode = AppMode.GUARDIAN
                )
            } catch (e: Exception) {
                _loginUiState.value = LoginUiState(
                    isLoading = false,
                    errorMessage = UiMessage.LOGIN_FAILED
                )
            }
        }
    }

    fun signup(email: String, displayName: String, password: String) {
        if (email.isBlank() || displayName.isBlank() || password.isBlank()) {
            _loginUiState.value = LoginUiState(
                errorMessage = UiMessage.SIGNUP_REQUIRED_FIELDS
            )
            return
        }

        viewModelScope.launch {
            _loginUiState.value = LoginUiState(isLoading = true)

            try {
                val appSession = appSessionRepository.appSessionState.first()
                val deviceId = appSession.device.deviceId

                if (deviceId.isNullOrBlank()) {
                    _loginUiState.value = LoginUiState(
                        isLoading = false,
                        errorMessage = UiMessage.DEVICE_ID_NOT_READY
                    )
                    return@launch
                }

                val response = authRepository.signup(
                    email = email.trim(),
                    password = password,
                    displayName = displayName.trim(),
                    deviceId = deviceId,
                    deviceName = android.os.Build.MODEL ?: AppDefaults.DEFAULT_DEVICE_NAME
                )

                if (!response.success || response.user == null || response.token.isNullOrBlank()) {
                    _loginUiState.value = LoginUiState(
                        isLoading = false,
                        errorMessage = UiMessage.SIGNUP_FAILED
                    )
                    return@launch
                }

                appSessionRepository.onLoginSuccess(
                    userId = response.user.id,
                    email = response.user.email ?: email.trim(),
                    displayName = response.user.display_name ?: displayName.trim(),
                    planType = response.user.plan_type ?: "free",
                    accessToken = response.token,
                    refreshToken = null,
                    tokenExpiresAt = null,
                    canActAsGuardian = true,
                    canActAsMember = true
                )

                _loginUiState.value = LoginUiState(
                    isLoading = false,
                    isSuccess = true,
                    selectedMode = AppMode.GUARDIAN
                )
            } catch (e: Exception) {
                _loginUiState.value = LoginUiState(
                    isLoading = false,
                    errorMessage = UiMessage.SIGNUP_FAILED
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