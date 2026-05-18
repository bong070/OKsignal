package com.bbksapps.oksignal.ui.member

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bbksapps.oksignal.data.local.repository.AppSessionRepository
import com.bbksapps.oksignal.data.local.repository.HeartbeatRepository
import com.bbksapps.oksignal.ui.common.UiMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MemberHomeViewModel(
    private val appSessionRepository: AppSessionRepository,
    private val heartbeatRepository: HeartbeatRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MemberHomeUiState())
    val uiState = _uiState.asStateFlow()

    fun checkIn() {
        viewModelScope.launch {

            _uiState.value = _uiState.value.copy(
                isCheckingIn = true,
                errorMessage = null
            )

            try {
                val appSession = appSessionRepository.appSessionState.first()

                val deviceId = appSession.device.deviceId

                if (deviceId.isNullOrBlank()) {
                    _uiState.value = _uiState.value.copy(
                        isCheckingIn = false,
                        errorMessage = UiMessage.DEVICE_ID_NOT_READY
                    )
                    return@launch
                }

                val success = heartbeatRepository.sendHeartbeat(deviceId)

                _uiState.value = _uiState.value.copy(
                    isCheckingIn = false,
                    lastCheckInSuccess = success,
                    errorMessage = if (success) null else UiMessage.UNKNOWN_ERROR
                )

            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isCheckingIn = false,
                    errorMessage = UiMessage.UNKNOWN_ERROR
                )
            }
        }
    }
}