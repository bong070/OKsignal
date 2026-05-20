package com.bbksapps.oksignal.data.local.repository

import com.bbksapps.oksignal.data.remote.ApiService
import com.bbksapps.oksignal.data.remote.HeartbeatRequest
import com.bbksapps.oksignal.data.remote.RetrofitProvider
import java.time.Instant

class HeartbeatRepository(
    private val apiService: ApiService = RetrofitProvider.api
) {
    suspend fun sendHeartbeat(
        deviceId: String,
        lastActivityAt: String = Instant.now().toString()
    ): Boolean {
        val response = apiService.sendHeartbeat(
            HeartbeatRequest(
                device_id = deviceId,
                last_activity_at = lastActivityAt
            )
        )

        return response.success
    }
}