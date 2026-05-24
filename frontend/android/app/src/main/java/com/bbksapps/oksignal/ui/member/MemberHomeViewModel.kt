package com.bbksapps.oksignal.ui.member

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bbksapps.oksignal.data.local.repository.AppSessionRepository
import com.bbksapps.oksignal.data.local.repository.DeviceStoreRepository
import com.bbksapps.oksignal.data.local.repository.HeartbeatRepository
import com.bbksapps.oksignal.data.local.repository.NeedHelpRepository
import com.bbksapps.oksignal.ui.common.UiMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.Instant

class MemberHomeViewModel(
    private val appSessionRepository: AppSessionRepository,
    private val heartbeatRepository: HeartbeatRepository,
    private val deviceStoreRepository: DeviceStoreRepository,
    private val needHelpRepository: NeedHelpRepository
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

                val now = Instant.now().toString()

                deviceStoreRepository.saveLastActivityAt(now)

                val success = heartbeatRepository.sendHeartbeat(
                    deviceId = deviceId,
                    lastActivityAt = now
                )

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

    fun sendNeedHelp() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isSendingHelp = true,
                errorMessage = null,
                needHelpSuccess = false
            )

            try {
                val appSession = appSessionRepository.appSessionState.first()
                val deviceId = appSession.device.deviceId

                if (deviceId.isNullOrBlank()) {
                    _uiState.value = _uiState.value.copy(
                        isSendingHelp = false,
                        errorMessage = UiMessage.DEVICE_ID_NOT_READY
                    )
                    return@launch
                }

                val success = needHelpRepository.sendNeedHelp(
                    deviceId = deviceId,
                    message = "Member requested help"
                )

                _uiState.value = _uiState.value.copy(
                    isSendingHelp = false,
                    needHelpSuccess = success,
                    errorMessage = if (success) null else UiMessage.UNKNOWN_ERROR
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isSendingHelp = false,
                    errorMessage = UiMessage.UNKNOWN_ERROR
                )
            }
        }
    }
}