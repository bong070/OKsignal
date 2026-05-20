package com.bbksapps.oksignal.ui.invite

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bbksapps.oksignal.data.local.repository.AppSessionRepository
import com.bbksapps.oksignal.data.repository.InviteRepository
import com.bbksapps.oksignal.ui.common.AppDefaults
import com.bbksapps.oksignal.ui.common.UiMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.bbksapps.oksignal.data.local.repository.DeviceStoreRepository
import java.time.Instant

class InviteAcceptViewModel(
    private val appSessionRepository: AppSessionRepository,
    private val inviteRepository: InviteRepository,
    private val deviceStoreRepository: DeviceStoreRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(InviteAcceptUiState())
    val uiState: StateFlow<InviteAcceptUiState> = _uiState.asStateFlow()

    init {
        observeDeviceId()
    }

    private fun observeDeviceId() {
        viewModelScope.launch {
            appSessionRepository.appSessionState.collect { appSession ->
                _uiState.value = _uiState.value.copy(
                    deviceId = appSession.device.deviceId
                )
            }
        }
    }

    fun acceptInvite(inviteToken: String) {
        viewModelScope.launch {
            val currentDeviceId = uiState.value.deviceId

            if (currentDeviceId.isNullOrBlank()) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = UiMessage.DEVICE_ID_NOT_READY_TRY_AGAIN
                )
                return@launch
            }

            val displayName = uiState.value.displayName.trim()

            if (displayName.isBlank()) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = UiMessage.ENTER_NAME
                )
                return@launch
            }

            _uiState.value = _uiState.value.copy(
                isLoading = true,
                errorMessage = null
            )

            try {
                val response = inviteRepository.acceptInvite(
                    token = inviteToken,
                    deviceId = currentDeviceId,
                    deviceName = android.os.Build.MODEL ?: AppDefaults.DEFAULT_DEVICE_NAME,
                    displayName = displayName
                )

                if (response.success) {
                    val memberUserId = response.member_user_id

                    if (memberUserId.isNullOrBlank()) {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = UiMessage.UNKNOWN_ERROR
                        )
                        return@launch
                    }

                    appSessionRepository.onLoginSuccess(
                        userId = memberUserId,
                        email = "",
                        displayName = displayName,
                        planType = "free",
                        accessToken = "",
                        refreshToken = null,
                        tokenExpiresAt = null,
                        canActAsGuardian = false,
                        canActAsMember = true
                    )

                    deviceStoreRepository.saveLastActivityAt(
                        Instant.now().toString()
                    )

                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isSuccess = true,
                        errorMessage = null
                    )
                }else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = UiMessage.ACCEPT_INVITE_FAILED
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = UiMessage.ACCEPT_INVITE_FAILED
                )
            }
        }
    }

    fun consumeSuccess() {
        _uiState.value = _uiState.value.copy(isSuccess = false)
    }

    fun onDisplayNameChange(value: String) {
        _uiState.value = _uiState.value.copy(displayName = value)
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}