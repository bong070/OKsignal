package com.bbksapps.oksignal.ui.start

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bbksapps.oksignal.data.local.model.AppMode
import com.bbksapps.oksignal.data.local.repository.AppSessionRepository
import com.bbksapps.oksignal.data.local.repository.DeviceStoreRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID
import kotlinx.coroutines.flow.first

sealed interface StartDestination {
    data object Loading : StartDestination
    data object Login : StartDestination
    data object GuardianHome : StartDestination
    data object MemberHome : StartDestination
}

class AppStartViewModel(
    private val appSessionRepository: AppSessionRepository,
    private val deviceStoreRepository: DeviceStoreRepository
) : ViewModel() {

    init {
        ensureDeviceId()
    }

    private fun ensureDeviceId() {
        viewModelScope.launch {
            val currentDeviceState = deviceStoreRepository.state.first()

            if (currentDeviceState.deviceId.isNullOrBlank()) {
                deviceStoreRepository.saveDeviceId(UUID.randomUUID().toString())
            }

            if (currentDeviceState.firstInstalledAt == null) {
                deviceStoreRepository.saveFirstInstalledAt(System.currentTimeMillis())
            }
        }
    }

    val startDestination: StateFlow<StartDestination> =
        appSessionRepository.appSessionState
            .map { appState ->
                if (!appState.isAuthenticated) {
                    StartDestination.Login
                } else {
                    when (appState.relationship.selectedMode) {
                        AppMode.GUARDIAN -> StartDestination.GuardianHome
                        AppMode.MEMBER -> StartDestination.MemberHome
                        AppMode.GROUP -> StartDestination.MemberHome
                    }
                }
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = StartDestination.Loading
            )
}