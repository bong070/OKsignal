package com.bbksapps.oksignal.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.TelephonyManager
import com.bbksapps.oksignal.data.local.repository.AppSessionRepository
import com.bbksapps.oksignal.data.local.repository.DeviceStoreRepository
import com.bbksapps.oksignal.data.local.repository.RelationshipStoreRepository
import com.bbksapps.oksignal.data.local.repository.SessionStoreRepository
import com.bbksapps.oksignal.data.local.repository.UserStoreRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.Instant

class MemberActivityReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return

        val shouldUpdate = when (action) {
            Intent.ACTION_USER_PRESENT -> true
            TelephonyManager.ACTION_PHONE_STATE_CHANGED -> {
                val state = intent.getStringExtra(TelephonyManager.EXTRA_STATE)
                state == TelephonyManager.EXTRA_STATE_OFFHOOK ||
                        state == TelephonyManager.EXTRA_STATE_IDLE
            }
            else -> false
        }

        if (!shouldUpdate) return

        val appContext = context.applicationContext

        CoroutineScope(Dispatchers.IO).launch {
            val deviceStoreRepository = DeviceStoreRepository(appContext)

            val appSessionRepository = AppSessionRepository(
                deviceStoreRepository = deviceStoreRepository,
                userStoreRepository = UserStoreRepository(appContext),
                sessionStoreRepository = SessionStoreRepository(appContext),
                relationshipStoreRepository = RelationshipStoreRepository(appContext)
            )

            val session = appSessionRepository.appSessionState.first()

            if (!session.relationship.canActAsMember) return@launch

            deviceStoreRepository.saveLastActivityAt(Instant.now().toString())
        }
    }
}