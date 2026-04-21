package com.bbksapps.oksignal.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import kotlinx.coroutines.delay
import com.bbksapps.oksignal.ui.start.StartDestination

@Composable
fun SplashScreen(
    startDestination: StartDestination?,
    onNavigateToLogin: () -> Unit,
    onNavigateToGuardian: () -> Unit,
    onNavigateToMember: () -> Unit,
    initialInviteToken: String?,
    onNavigateToInviteAccept: (String) -> Unit,
) {
    LaunchedEffect(Unit) {
        delay(1000)

        if (!initialInviteToken.isNullOrBlank()) {
            onNavigateToInviteAccept(initialInviteToken)
            return@LaunchedEffect
        }

        when (startDestination) {
            StartDestination.GuardianHome -> onNavigateToGuardian()
            StartDestination.MemberHome -> onNavigateToMember()
            StartDestination.Login -> onNavigateToLogin()
            null -> Unit
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            color = MaterialTheme.colorScheme.primary
        )
    }
}