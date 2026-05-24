package com.bbksapps.oksignal.data.local.repository

import com.bbksapps.oksignal.data.remote.FcmTokenRequest
import com.bbksapps.oksignal.data.remote.RetrofitProvider

class FcmTokenRepository {

    private val apiService = RetrofitProvider.api

    suspend fun updateFcmToken(
        deviceId: String,
        fcmToken: String
    ): Boolean {
        val response = apiService.updateFcmToken(
            FcmTokenRequest(
                device_id = deviceId,
                fcm_token = fcmToken
            )
        )

        return response.success
    }
}