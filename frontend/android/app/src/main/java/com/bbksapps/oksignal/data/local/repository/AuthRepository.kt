package com.bbksapps.oksignal.data.repository

import com.bbksapps.oksignal.data.remote.AuthLoginRequest
import com.bbksapps.oksignal.data.remote.AuthResponse
import com.bbksapps.oksignal.data.remote.AuthSignupRequest
import com.bbksapps.oksignal.data.remote.RetrofitProvider
import com.google.gson.Gson
import retrofit2.HttpException

class AuthRepository {

    suspend fun login(
        email: String,
        password: String,
        deviceId: String,
        deviceName: String?
    ): AuthResponse {
        return try {
            RetrofitProvider.api.login(
                AuthLoginRequest(
                    email = email,
                    password = password,
                    device_id = deviceId,
                    device_name = deviceName
                )
            )
        } catch (e: HttpException) {
            val errorBody = e.response()?.errorBody()?.string()

            val parsedError = try {
                Gson().fromJson(errorBody, ErrorResponse::class.java)
            } catch (_: Exception) {
                null
            }

            AuthResponse(
                success = false,
                error = parsedError?.error ?: "Login failed"
            )
        }
    }

    suspend fun signup(
        email: String,
        password: String,
        displayName: String,
        deviceId: String,
        deviceName: String?
    ): AuthResponse {
        return try {
            RetrofitProvider.api.signup(
                AuthSignupRequest(
                    email = email,
                    password = password,
                    display_name = displayName,
                    device_id = deviceId,
                    device_name = deviceName
                )
            )
        } catch (e: HttpException) {
            val errorBody = e.response()?.errorBody()?.string()

            val parsedError = try {
                Gson().fromJson(errorBody, ErrorResponse::class.java)
            } catch (_: Exception) {
                null
            }

            AuthResponse(
                success = false,
                error = parsedError?.error ?: "Signup failed"
            )
        }
    }
}

private data class ErrorResponse(
    val success: Boolean? = null,
    val error: String? = null
)