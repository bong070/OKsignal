package com.bbksapps.oksignal.data.local.repository

import android.content.Context
import androidx.datastore.preferences.core.edit
import com.bbksapps.oksignal.data.local.datastore.RelationshipPreferencesKeys
import com.bbksapps.oksignal.data.local.datastore.relationshipDataStore
import com.bbksapps.oksignal.data.local.model.AppMode
import com.bbksapps.oksignal.data.local.model.RelationshipState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RelationshipStoreRepository(context: Context) {

    private val appContext = context.applicationContext
    private val dataStore = appContext.relationshipDataStore

    val state: Flow<RelationshipState> = dataStore.data.map { prefs ->
        RelationshipState(
            canActAsGuardian = prefs[RelationshipPreferencesKeys.CAN_ACT_AS_GUARDIAN] ?: false,
            canActAsMember = prefs[RelationshipPreferencesKeys.CAN_ACT_AS_MEMBER] ?: false,
            selectedMode = AppMode.fromValue(prefs[RelationshipPreferencesKeys.SELECTED_MODE]),
            selectedMemberUserId = prefs[RelationshipPreferencesKeys.SELECTED_MEMBER_USER_ID],
            selectedGuardianUserId = prefs[RelationshipPreferencesKeys.SELECTED_GUARDIAN_USER_ID],
            selectedGroupId = prefs[RelationshipPreferencesKeys.SELECTED_GROUP_ID]
        )
    }

    suspend fun saveCapabilities(
        canActAsGuardian: Boolean,
        canActAsMember: Boolean
    ) {
        dataStore.edit { prefs ->
            prefs[RelationshipPreferencesKeys.CAN_ACT_AS_GUARDIAN] = canActAsGuardian
            prefs[RelationshipPreferencesKeys.CAN_ACT_AS_MEMBER] = canActAsMember
        }
    }

    suspend fun setSelectedMode(mode: AppMode) {
        dataStore.edit { prefs ->
            prefs[RelationshipPreferencesKeys.SELECTED_MODE] = mode.name
        }
    }

    suspend fun setSelectedMemberUserId(userId: String?) {
        dataStore.edit { prefs ->
            if (userId.isNullOrBlank()) {
                prefs.remove(RelationshipPreferencesKeys.SELECTED_MEMBER_USER_ID)
            } else {
                prefs[RelationshipPreferencesKeys.SELECTED_MEMBER_USER_ID] = userId
            }
        }
    }

    suspend fun setSelectedGuardianUserId(userId: String?) {
        dataStore.edit { prefs ->
            if (userId.isNullOrBlank()) {
                prefs.remove(RelationshipPreferencesKeys.SELECTED_GUARDIAN_USER_ID)
            } else {
                prefs[RelationshipPreferencesKeys.SELECTED_GUARDIAN_USER_ID] = userId
            }
        }
    }

    suspend fun setSelectedGroupId(groupId: String?) {
        dataStore.edit { prefs ->
            if (groupId.isNullOrBlank()) {
                prefs.remove(RelationshipPreferencesKeys.SELECTED_GROUP_ID)
            } else {
                prefs[RelationshipPreferencesKeys.SELECTED_GROUP_ID] = groupId
            }
        }
    }

    suspend fun clear() {
        dataStore.edit { it.clear() }
    }
}