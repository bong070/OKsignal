package com.bbksapps.oksignal.ui.start

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bbksapps.oksignal.data.local.model.AppMode
import com.bbksapps.oksignal.data.local.repository.AppSessionRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

sealed interface StartDestination {
    data object Login : StartDestination
    data object GuardianHome : StartDestination
    data object MemberHome : StartDestination
}

class AppStartViewModel(
    appSessionRepository: AppSessionRepository
) : ViewModel() {

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
                initialValue = StartDestination.Login
            )
}