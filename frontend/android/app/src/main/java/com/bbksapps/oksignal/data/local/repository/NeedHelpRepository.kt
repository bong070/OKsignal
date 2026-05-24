package com.bbksapps.oksignal.data.local.repository

import com.bbksapps.oksignal.data.remote.NeedHelpRequest
import com.bbksapps.oksignal.data.remote.RetrofitProvider

class NeedHelpRepository {

    private val apiService = RetrofitProvider.api

    suspend fun sendNeedHelp(
        deviceId: String,
        message: String? = null
    ): Boolean {
        val response = apiService.sendNeedHelp(
            NeedHelpRequest(
                device_id = deviceId,
                message = message
            )
        )

        return response.success
    }
}