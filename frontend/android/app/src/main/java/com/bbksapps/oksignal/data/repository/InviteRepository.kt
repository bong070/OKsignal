package com.bbksapps.oksignal.data.repository

import com.bbksapps.oksignal.data.remote.AcceptInviteRequest
import com.bbksapps.oksignal.data.remote.AcceptInviteResponse
import com.bbksapps.oksignal.data.remote.CreateInviteRequest
import com.bbksapps.oksignal.data.remote.CreateInviteResponse
import com.bbksapps.oksignal.data.remote.RetrofitProvider

class InviteRepository {

    suspend fun createInvite(guardianUserId: String): CreateInviteResponse {
        return RetrofitProvider.api.createInvite(
            CreateInviteRequest(guardian_user_id = guardianUserId)
        )
    }

    suspend fun acceptInvite(
        token: String,
        deviceId: String,
        deviceName: String? = null,
        displayName: String? = null
    ): AcceptInviteResponse {
        return RetrofitProvider.api.acceptInvite(
            AcceptInviteRequest(
                token = token,
                device_id = deviceId,
                device_name = deviceName,
                display_name = displayName
            )
        )
    }
}