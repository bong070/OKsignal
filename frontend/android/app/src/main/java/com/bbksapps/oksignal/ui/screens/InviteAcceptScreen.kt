package com.bbksapps.oksignal.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.bbksapps.oksignal.ui.invite.InviteAcceptUiState
import com.bbksapps.oksignal.ui.theme.Dimens

@Composable
fun InviteAcceptScreen(
    inviteToken: String,
    uiState: InviteAcceptUiState,
    onAcceptClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(Dimens.SpaceLg),
        verticalArrangement = Arrangement.spacedBy(Dimens.SpaceMd)
    ) {
        Text(
            text = "Join OKSignal",
            style = MaterialTheme.typography.headlineSmall
        )

        Text(
            text = "Invite token: $inviteToken",
            style = MaterialTheme.typography.bodyMedium
        )

        Text(
            text = "Device ID: ${uiState.deviceId ?: "Preparing device..."}",
            style = MaterialTheme.typography.bodySmall
        )

        uiState.errorMessage?.let { error ->
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        Button(
            onClick = onAcceptClick,
            enabled = !uiState.isLoading && !uiState.deviceId.isNullOrBlank(),
            modifier = Modifier.fillMaxWidth()
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator()
            } else {
                Text("Accept Invite")
            }
        }
    }
}