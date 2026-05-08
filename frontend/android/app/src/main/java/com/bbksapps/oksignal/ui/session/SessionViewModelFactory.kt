package com.bbksapps.oksignal.ui.session

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.bbksapps.oksignal.data.local.repository.AppSessionRepository
import com.bbksapps.oksignal.data.repository.AuthRepository

class SessionViewModelFactory(
    private val appSessionRepository: AppSessionRepository,
    private val authRepository: AuthRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SessionViewModel::class.java)) {
            return SessionViewModel(
                appSessionRepository = appSessionRepository,
                authRepository = authRepository
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}