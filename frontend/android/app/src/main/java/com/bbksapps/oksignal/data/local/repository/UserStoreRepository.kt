package com.bbksapps.oksignal.data.local.repository

import android.content.Context
import androidx.datastore.preferences.core.edit
import com.bbksapps.oksignal.data.local.datastore.UserPreferencesKeys
import com.bbksapps.oksignal.data.local.datastore.userDataStore
import com.bbksapps.oksignal.data.local.model.UserState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class UserStoreRepository(context: Context) {

    private val appContext = context.applicationContext
    private val dataStore = appContext.userDataStore

    val state: Flow<UserState> = dataStore.data.map { prefs ->
        UserState(
            userId = prefs[UserPreferencesKeys.USER_ID],
            email = prefs[UserPreferencesKeys.EMAIL],
            displayName = prefs[UserPreferencesKeys.DISPLAY_NAME],
            planType = prefs[UserPreferencesKeys.PLAN_TYPE]
        )
    }

    suspend fun saveUser(
        userId: String,
        email: String,
        displayName: String,
        planType: String?
    ) {
        dataStore.edit { prefs ->
            prefs[UserPreferencesKeys.USER_ID] = userId
            prefs[UserPreferencesKeys.EMAIL] = email
            prefs[UserPreferencesKeys.DISPLAY_NAME] = displayName

            if (planType.isNullOrBlank()) {
                prefs.remove(UserPreferencesKeys.PLAN_TYPE)
            } else {
                prefs[UserPreferencesKeys.PLAN_TYPE] = planType
            }
        }
    }

    suspend fun updateDisplayName(displayName: String) {
        dataStore.edit { prefs ->
            prefs[UserPreferencesKeys.DISPLAY_NAME] = displayName
        }
    }

    suspend fun updatePlanType(planType: String?) {
        dataStore.edit { prefs ->
            if (planType.isNullOrBlank()) {
                prefs.remove(UserPreferencesKeys.PLAN_TYPE)
            } else {
                prefs[UserPreferencesKeys.PLAN_TYPE] = planType
            }
        }
    }

    suspend fun clear() {
        dataStore.edit { it.clear() }
    }
}