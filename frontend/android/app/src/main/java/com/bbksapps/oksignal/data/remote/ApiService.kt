package com.bbksapps.oksignal.data.remote

import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiService {
    @POST("register")
    suspend fun register(
        @Body request: RegisterRequest
    ): RegisterResponse

    @POST("auth/login")
    suspend fun login(
        @Body request: AuthLoginRequest
    ): AuthResponse

    @POST("auth/signup")
    suspend fun signup(
        @Body request: AuthSignupRequest
    ): AuthResponse

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