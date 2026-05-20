package com.bbksapps.oksignal.ui.invite

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.bbksapps.oksignal.data.local.repository.AppSessionRepository
import com.bbksapps.oksignal.data.local.repository.DeviceStoreRepository
import com.bbksapps.oksignal.data.repository.InviteRepository

class InviteAcceptViewModelFactory(
    private val appSessionRepository: AppSessionRepository,
    private val inviteRepository: InviteRepository,
    private val deviceStoreRepository: DeviceStoreRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(InviteAcceptViewModel::class.java)) {
            return InviteAcceptViewModel(
                appSessionRepository = appSessionRepository,
                inviteRepository = inviteRepository,
                deviceStoreRepository = deviceStoreRepository
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}