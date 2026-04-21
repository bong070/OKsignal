package com.bbksapps.oksignal.data.local.datastore

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey

object RelationshipPreferencesKeys {
    val CAN_ACT_AS_GUARDIAN = booleanPreferencesKey("can_act_as_guardian")
    val CAN_ACT_AS_MEMBER = booleanPreferencesKey("can_act_as_member")

    // "guardian", "member", "group"
    val SELECTED_MODE = stringPreferencesKey("selected_mode")

    // currently selected target/member/group for UI context
    val SELECTED_MEMBER_USER_ID = stringPreferencesKey("selected_member_user_id")
    val SELECTED_GUARDIAN_USER_ID = stringPreferencesKey("selected_guardian_user_id")
    val SELECTED_GROUP_ID = stringPreferencesKey("selected_group_id")
}