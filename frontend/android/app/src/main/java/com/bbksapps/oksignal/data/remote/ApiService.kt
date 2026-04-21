package com.bbksapps.oksignal.data.remote

import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiService {
    @POST("invites/create")
    suspend fun createInvite(
        @Body request: CreateInviteRequest
    ): CreateInviteResponse

    @POST("invites/accept")
    suspend fun acceptInvite(
        @Body request: AcceptInviteRequest
    ): AcceptInviteResponse

    @GET("guardians/members")
    suspend fun getGuardianMembers(
        @Query("guardian_user_id") guardianUserId: String
    ): GuardianMembersResponse
}