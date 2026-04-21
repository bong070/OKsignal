package com.bbksapps.oksignal.data.local.datastore

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore

val Context.deviceDataStore by preferencesDataStore(name = "device_prefs")
val Context.userDataStore by preferencesDataStore(name = "user_prefs")
val Context.sessionDataStore by preferencesDataStore(name = "session_prefs")
val Context.relationshipDataStore by preferencesDataStore(name = "relationship_prefs")