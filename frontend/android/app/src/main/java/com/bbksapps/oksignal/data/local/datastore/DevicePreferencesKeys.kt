package com.bbksapps.oksignal.data.local.datastore

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey

object DevicePreferencesKeys {
    val DEVICE_ID = stringPreferencesKey("device_id")
    val FCM_TOKEN = stringPreferencesKey("fcm_token")
    val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
    val FIRST_INSTALLED_AT = longPreferencesKey("first_installed_at")
    val LAST_APP_VERSION = stringPreferencesKey("last_app_version")
}