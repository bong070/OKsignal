package com.bbksapps.oksignal.worker

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.bbksapps.oksignal.data.local.repository.AppSessionRepository
import com.bbksapps.oksignal.data.local.repository.DeviceStoreRepository
import com.bbksapps.oksignal.data.local.repository.RelationshipStoreRepository
import com.bbksapps.oksignal.data.local.repository.SessionStoreRepository
import com.bbksapps.oksignal.data.local.repository.UserStoreRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return

        val appContext = context.applicationContext

        CoroutineScope(Dispatchers.IO).launch {

            val appSessionRepository = AppSessionRepository(
                deviceStoreRepository = DeviceStoreRepository(appContext),
                userStoreRepository = UserStoreRepository(appContext),
                sessionStoreRepository = SessionStoreRepository(appContext),
                relationshipStoreRepository = RelationshipStoreRepository(appContext)
            )

            val session = appSessionRepository.appSessionState.first()

            if (session.relationship.canActAsMember) {
                HeartbeatScheduler.start(appContext)
            }
        }
    }
}