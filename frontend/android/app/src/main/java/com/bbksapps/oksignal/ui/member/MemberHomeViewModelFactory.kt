package com.bbksapps.oksignal.ui.member

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.bbksapps.oksignal.data.local.repository.AppSessionRepository
import com.bbksapps.oksignal.data.local.repository.DeviceStoreRepository
import com.bbksapps.oksignal.data.local.repository.HeartbeatRepository
import com.bbksapps.oksignal.data.local.repository.NeedHelpRepository

class MemberHomeViewModelFactory(
    private val appSessionRepository: AppSessionRepository,
    private val heartbeatRepository: HeartbeatRepository,
    private val deviceStoreRepository: DeviceStoreRepository,
    private val needHelpRepository: NeedHelpRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MemberHomeViewModel::class.java)) {
            return MemberHomeViewModel(
                appSessionRepository = appSessionRepository,
                heartbeatRepository = heartbeatRepository,
                deviceStoreRepository = deviceStoreRepository,
                needHelpRepository = needHelpRepository
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}