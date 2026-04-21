package com.bbksapps.oksignal.data.local.model

data class DeviceState(
    val deviceId: String? = null,
    val fcmToken: String? = null,
    val onboardingCompleted: Boolean = false,
    val firstInstalledAt: Long? = null,
    val lastAppVersion: String? = null
)