package com.bbksapps.oksignal.navigation

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.bbksapps.oksignal.data.local.repository.AppSessionRepository
import com.bbksapps.oksignal.data.local.repository.AuthRepository
import com.bbksapps.oksignal.data.local.repository.DeviceStoreRepository
import com.bbksapps.oksignal.data.local.repository.HeartbeatRepository
import com.bbksapps.oksignal.data.local.repository.RelationshipStoreRepository
import com.bbksapps.oksignal.data.local.repository.SessionStoreRepository
import com.bbksapps.oksignal.data.local.repository.UserStoreRepository

data class AppDependencies(
    val deviceStoreRepository: DeviceStoreRepository,
    val userStoreRepository: UserStoreRepository,
    val sessionStoreRepository: SessionStoreRepository,
    val relationshipStoreRepository: RelationshipStoreRepository,
    val appSessionRepository: AppSessionRepository,
    val authRepository: AuthRepository,
    val heartbeatRepository: HeartbeatRepository
)

@Composable
fun rememberAppDependencies(): AppDependencies {
    val context = LocalContext.current
    val appContext = context.applicationContext

    val deviceStoreRepository = remember(appContext) {
        DeviceStoreRepository(appContext)
    }
    val userStoreRepository = remember(appContext) {
        UserStoreRepository(appContext)
    }
    val sessionStoreRepository = remember(appContext) {
        SessionStoreRepository(appContext)
    }
    val relationshipStoreRepository = remember(appContext) {
        RelationshipStoreRepository(appContext)
    }

    val authRepository = remember {
        AuthRepository()
    }

    val heartbeatRepository = remember {
        HeartbeatRepository()
    }

    val appSessionRepository = remember(
        deviceStoreRepository,
        userStoreRepository,
        sessionStoreRepository,
        relationshipStoreRepository
    ) {
        AppSessionRepository(
            deviceStoreRepository = deviceStoreRepository,
            userStoreRepository = userStoreRepository,
            sessionStoreRepository = sessionStoreRepository,
            relationshipStoreRepository = relationshipStoreRepository
        )
    }

    return remember(
        deviceStoreRepository,
        userStoreRepository,
        sessionStoreRepository,
        relationshipStoreRepository,
        appSessionRepository,
        authRepository,
        heartbeatRepository
    ) {
        AppDependencies(
            deviceStoreRepository = deviceStoreRepository,
            userStoreRepository = userStoreRepository,
            sessionStoreRepository = sessionStoreRepository,
            relationshipStoreRepository = relationshipStoreRepository,
            appSessionRepository = appSessionRepository,
            authRepository = authRepository,
            heartbeatRepository = heartbeatRepository
        )
    }
}