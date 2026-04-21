package com.bbksapps.oksignal.data.local.datastore

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey

object SessionPreferencesKeys {
    val ACCESS_TOKEN = stringPreferencesKey("access_token")
    val REFRESH_TOKEN = stringPreferencesKey("refresh_token")
    val TOKEN_EXPIRES_AT = longPreferencesKey("token_expires_at")
    val IS_LOGGED_IN = booleanPreferencesKey("is_logged_in")
    val LAST_AUTHENTICATED_AT = longPreferencesKey("last_authenticated_at")
}