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

@Composable
fun SplashScreen(
    onNavigateToLogin: () -> Unit,
    onNavigateToGuardian: () -> Unit,
    onNavigateToMember: () -> Unit
) {
    LaunchedEffect(Unit) {
        delay(1000)

        // TODO: 나중에 여기 device_id + 서버 체크 들어감
        val mockState = "guardian" // "guardian", "member", "unknown"

        when (mockState) {
            "guardian" -> onNavigateToGuardian()
            "member" -> onNavigateToMember()
            else -> onNavigateToLogin()
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