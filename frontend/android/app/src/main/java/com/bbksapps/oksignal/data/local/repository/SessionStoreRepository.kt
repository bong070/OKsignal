package com.bbksapps.oksignal.data.local.repository

import android.content.Context
import androidx.datastore.preferences.core.edit
import com.bbksapps.oksignal.data.local.datastore.SessionPreferencesKeys
import com.bbksapps.oksignal.data.local.datastore.sessionDataStore
import com.bbksapps.oksignal.data.local.model.SessionState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SessionStoreRepository(context: Context) {

    private val appContext = context.applicationContext
    private val dataStore = appContext.sessionDataStore

    val state: Flow<SessionState> = dataStore.data.map { prefs ->
        SessionState(
            accessToken = prefs[SessionPreferencesKeys.ACCESS_TOKEN],
            refreshToken = prefs[SessionPreferencesKeys.REFRESH_TOKEN],
            tokenExpiresAt = prefs[SessionPreferencesKeys.TOKEN_EXPIRES_AT],
            isLoggedIn = prefs[SessionPreferencesKeys.IS_LOGGED_IN] ?: false,
            lastAuthenticatedAt = prefs[SessionPreferencesKeys.LAST_AUTHENTICATED_AT]
        )
    }

    suspend fun saveSession(
        accessToken: String,
        refreshToken: String? = null,
        tokenExpiresAt: Long? = null
    ) {
        dataStore.edit { prefs ->
            prefs[SessionPreferencesKeys.ACCESS_TOKEN] = accessToken

            if (refreshToken.isNullOrBlank()) {
                prefs.remove(SessionPreferencesKeys.REFRESH_TOKEN)
            } else {
                prefs[SessionPreferencesKeys.REFRESH_TOKEN] = refreshToken
            }

            if (tokenExpiresAt == null) {
                prefs.remove(SessionPreferencesKeys.TOKEN_EXPIRES_AT)
            } else {
                prefs[SessionPreferencesKeys.TOKEN_EXPIRES_AT] = tokenExpiresAt
            }

            prefs[SessionPreferencesKeys.IS_LOGGED_IN] = true
            prefs[SessionPreferencesKeys.LAST_AUTHENTICATED_AT] = System.currentTimeMillis()
        }
    }

    suspend fun updateAccessToken(
        accessToken: String,
        tokenExpiresAt: Long? = null
    ) {
        dataStore.edit { prefs ->
            prefs[SessionPreferencesKeys.ACCESS_TOKEN] = accessToken

            if (tokenExpiresAt == null) {
                prefs.remove(SessionPreferencesKeys.TOKEN_EXPIRES_AT)
            } else {
                prefs[SessionPreferencesKeys.TOKEN_EXPIRES_AT] = tokenExpiresAt
            }

            prefs[SessionPreferencesKeys.IS_LOGGED_IN] = true
            prefs[SessionPreferencesKeys.LAST_AUTHENTICATED_AT] = System.currentTimeMillis()
        }
    }

    suspend fun clear() {
        dataStore.edit { it.clear() }
    }
}