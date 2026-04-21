package com.bbksapps.oksignal.data.local.model

data class SessionState(
    val accessToken: String? = null,
    val refreshToken: String? = null,
    val tokenExpiresAt: Long? = null,
    val isLoggedIn: Boolean = false,
    val lastAuthenticatedAt: Long? = null
)