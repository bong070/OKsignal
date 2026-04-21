package com.bbksapps.oksignal.ui.invite

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bbksapps.oksignal.data.local.repository.AppSessionRepository
import com.bbksapps.oksignal.data.repository.InviteRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class InviteAcceptViewModel(
    private val appSessionRepository: AppSessionRepository,
    private val inviteRepository: InviteRepository
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
                    errorMessage = "Device ID is not ready yet. Please try again."
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
                    deviceName = android.os.Build.MODEL ?: "Android Device"
                )

                if (response.success) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isSuccess = true,
                        errorMessage = null
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = response.error ?: "Failed to accept invite."
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "Failed to accept invite."
                )
            }
        }
    }

    fun consumeSuccess() {
        _uiState.value = _uiState.value.copy(isSuccess = false)
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}