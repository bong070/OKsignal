package com.bbksapps.oksignal.navigation

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.bbksapps.oksignal.data.local.repository.AppSessionRepository
import com.bbksapps.oksignal.data.local.repository.DeviceStoreRepository
import com.bbksapps.oksignal.data.local.repository.RelationshipStoreRepository
import com.bbksapps.oksignal.data.local.repository.SessionStoreRepository
import com.bbksapps.oksignal.data.local.repository.UserStoreRepository
import com.bbksapps.oksignal.data.repository.AuthRepository

data class AppDependencies(
    val deviceStoreRepository: DeviceStoreRepository,
    val userStoreRepository: UserStoreRepository,
    val sessionStoreRepository: SessionStoreRepository,
    val relationshipStoreRepository: RelationshipStoreRepository,
    val appSessionRepository: AppSessionRepository,
    val authRepository: AuthRepository
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
        appSessionRepository
    ) {
        AppDependencies(
            deviceStoreRepository = deviceStoreRepository,
            userStoreRepository = userStoreRepository,
            sessionStoreRepository = sessionStoreRepository,
            relationshipStoreRepository = relationshipStoreRepository,
            appSessionRepository = appSessionRepository,
            authRepository = authRepository
        )
    }
}