package com.bbksapps.oksignal.ui.guardian

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.bbksapps.oksignal.data.local.repository.AppSessionRepository
import com.bbksapps.oksignal.data.repository.GuardianRepository
import com.bbksapps.oksignal.data.repository.InviteRepository

class GuardianHomeViewModelFactory(
    private val appSessionRepository: AppSessionRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(GuardianHomeViewModel::class.java)) {
            return GuardianHomeViewModel(
                appSessionRepository = appSessionRepository,
                guardianRepository = GuardianRepository(),
                inviteRepository = InviteRepository()
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}