package com.bbksapps.oksignal.ui.start

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.bbksapps.oksignal.data.local.repository.AppSessionRepository

class AppStartViewModelFactory(
    private val appSessionRepository: AppSessionRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AppStartViewModel::class.java)) {
            return AppStartViewModel(appSessionRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}