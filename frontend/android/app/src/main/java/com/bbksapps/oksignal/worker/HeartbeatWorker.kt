package com.bbksapps.oksignal.worker

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.bbksapps.oksignal.data.local.repository.AppSessionRepository
import com.bbksapps.oksignal.data.local.repository.DeviceStoreRepository
import com.bbksapps.oksignal.data.local.repository.HeartbeatRepository
import com.bbksapps.oksignal.data.local.repository.RelationshipStoreRepository
import com.bbksapps.oksignal.data.local.repository.SessionStoreRepository
import com.bbksapps.oksignal.data.local.repository.UserStoreRepository
import kotlinx.coroutines.flow.first
import java.time.Instant
import com.bbksapps.oksignal.activity.UsageStatsActivityDetector

class HeartbeatWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            val context = applicationContext

            val appSessionRepository = AppSessionRepository(
                deviceStoreRepository = DeviceStoreRepository(context),
                userStoreRepository = UserStoreRepository(context),
                sessionStoreRepository = SessionStoreRepository(context),
                relationshipStoreRepository = RelationshipStoreRepository(context)
            )

            val session = appSessionRepository.appSessionState.first()

            val deviceId = session.device.deviceId
            val canActAsMember = session.relationship.canActAsMember

            Log.d(
                "OKSignalWorker",
                "deviceId=$deviceId, canActAsMember=$canActAsMember, lastActivityAt=${session.device.lastActivityAt}"
            )

            if (deviceId.isNullOrBlank() || !canActAsMember) {
                Log.d("OKSignalWorker", "Skipped: deviceId blank or not member")
                return Result.success()
            }

            val usageDetectedAtMillis =
                UsageStatsActivityDetector(context).getLastUserActivityTimeMillis()

            val savedLastActivityAt = session.device.lastActivityAt

            val usageDetectedAt = usageDetectedAtMillis
                ?.let { Instant.ofEpochMilli(it).toString() }

            val lastActivityAt = listOfNotNull(
                savedLastActivityAt,
                usageDetectedAt
            ).maxOrNull()

            if (lastActivityAt.isNullOrBlank()) {
                Log.d("OKSignalWorker", "Skipped: lastActivityAt blank")
                return Result.success()
            }

            if (usageDetectedAt != null && usageDetectedAt != savedLastActivityAt) {
                DeviceStoreRepository(context).saveLastActivityAt(lastActivityAt)
            }

            HeartbeatRepository().sendHeartbeat(
                deviceId = deviceId,
                lastActivityAt = lastActivityAt
            )
            Log.d("OKSignalWorker", "Heartbeat sent successfully")

            Result.success()

        } catch (e: Exception) {
            Log.e("OKSignalWorker", "Heartbeat failed", e)
            Result.retry()
        }
    }
}