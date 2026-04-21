package com.bbksapps.oksignal.data.local.datastore

import androidx.datastore.preferences.core.stringPreferencesKey

object UserPreferencesKeys {
    val USER_ID = stringPreferencesKey("user_id")
    val EMAIL = stringPreferencesKey("email")
    val DISPLAY_NAME = stringPreferencesKey("display_name")
    val PLAN_TYPE = stringPreferencesKey("plan_type")
}