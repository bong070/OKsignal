package com.bbksapps.oksignal.data.local.repository

import android.content.Context
import com.bbksapps.oksignal.data.local.datastore.DevicePreferencesKeys
import com.bbksapps.oksignal.data.local.model.DeviceState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import androidx.datastore.preferences.core.edit
import com.bbksapps.oksignal.data.local.datastore.deviceDataStore

class DeviceStoreRepository(context: Context) {

    private val appContext = context.applicationContext
    private val dataStore = appContext.deviceDataStore

    val state: Flow<DeviceState> = dataStore.data.map { prefs ->
        DeviceState(
            deviceId = prefs[DevicePreferencesKeys.DEVICE_ID],
            fcmToken = prefs[DevicePreferencesKeys.FCM_TOKEN],
            onboardingCompleted = prefs[DevicePreferencesKeys.ONBOARDING_COMPLETED] ?: false,
            firstInstalledAt = prefs[DevicePreferencesKeys.FIRST_INSTALLED_AT],
            lastAppVersion = prefs[DevicePreferencesKeys.LAST_APP_VERSION],
            lastActivityAt = prefs[DevicePreferencesKeys.LAST_ACTIVITY_AT]
        )
    }

    suspend fun saveDeviceId(deviceId: String) {
        dataStore.edit { prefs ->
            prefs[DevicePreferencesKeys.DEVICE_ID] = deviceId
        }
    }

    suspend fun saveFcmToken(token: String) {
        dataStore.edit { prefs ->
            prefs[DevicePreferencesKeys.FCM_TOKEN] = token
        }
    }

    suspend fun setOnboardingCompleted(completed: Boolean) {
        dataStore.edit { prefs ->
            prefs[DevicePreferencesKeys.ONBOARDING_COMPLETED] = completed
        }
    }

    suspend fun saveFirstInstalledAt(timestamp: Long) {
        dataStore.edit { prefs ->
            prefs[DevicePreferencesKeys.FIRST_INSTALLED_AT] = timestamp
        }
    }

    suspend fun saveLastAppVersion(version: String) {
        dataStore.edit { prefs ->
            prefs[DevicePreferencesKeys.LAST_APP_VERSION] = version
        }
    }

    suspend fun clearAll() {
        dataStore.edit { it.clear() }
    }

    suspend fun saveLastActivityAt(timestamp: String) {
        dataStore.edit { prefs ->
            prefs[DevicePreferencesKeys.LAST_ACTIVITY_AT] = timestamp
        }
    }
}