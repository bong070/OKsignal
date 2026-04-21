package com.bbksapps.oksignal.data.local.repository

import com.bbksapps.oksignal.data.local.model.AppMode
import com.bbksapps.oksignal.data.local.model.AppSessionState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class AppSessionRepository(
    private val deviceStoreRepository: DeviceStoreRepository,
    private val userStoreRepository: UserStoreRepository,
    private val sessionStoreRepository: SessionStoreRepository,
    private val relationshipStoreRepository: RelationshipStoreRepository
) {

    val appSessionState: Flow<AppSessionState> = combine(
        deviceStoreRepository.state,
        userStoreRepository.state,
        sessionStoreRepository.state,
        relationshipStoreRepository.state
    ) { device, user, session, relationship ->
        AppSessionState(
            device = device,
            user = user,
            session = session,
            relationship = relationship
        )
    }

    suspend fun onLoginSuccess(
        userId: String,
        email: String,
        displayName: String,
        planType: String?,
        accessToken: String,
        refreshToken: String?,
        tokenExpiresAt: Long?,
        canActAsGuardian: Boolean,
        canActAsMember: Boolean
    ) {
        userStoreRepository.saveUser(
            userId = userId,
            email = email,
            displayName = displayName,
            planType = planType
        )

        sessionStoreRepository.saveSession(
            accessToken = accessToken,
            refreshToken = refreshToken,
            tokenExpiresAt = tokenExpiresAt
        )

        relationshipStoreRepository.saveCapabilities(
            canActAsGuardian = canActAsGuardian,
            canActAsMember = canActAsMember
        )

        val defaultMode = when {
            canActAsGuardian -> AppMode.GUARDIAN
            canActAsMember -> AppMode.MEMBER
            else -> AppMode.MEMBER
        }

        relationshipStoreRepository.setSelectedMode(defaultMode)
    }

    suspend fun logout() {
        sessionStoreRepository.clear()
        userStoreRepository.clear()
        relationshipStoreRepository.clear()
        // device는 유지
    }
}