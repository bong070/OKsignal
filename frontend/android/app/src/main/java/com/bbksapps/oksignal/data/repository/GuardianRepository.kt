package com.bbksapps.oksignal.data.repository

import com.bbksapps.oksignal.data.remote.GuardianMembersResponse
import com.bbksapps.oksignal.data.remote.RetrofitProvider

class GuardianRepository {
    suspend fun getGuardianMembers(guardianUserId: String): GuardianMembersResponse {
        return RetrofitProvider.api.getGuardianMembers(guardianUserId)
    }
}