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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.ui.res.stringResource
import com.bbksapps.oksignal.R
import com.bbksapps.oksignal.ui.common.asString

@Composable
fun InviteAcceptScreen(
    inviteToken: String,
    uiState: InviteAcceptUiState,
    onDisplayNameChange: (String) -> Unit,
    onAcceptClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(Dimens.SpaceLg),
        verticalArrangement = Arrangement.spacedBy(Dimens.SpaceMd)
    ) {
        val preparingDeviceText = stringResource(R.string.preparing_device)
        val deviceIdText = uiState.deviceId ?: preparingDeviceText

        Text(
            text = stringResource(R.string.join_oksignal),
            style = MaterialTheme.typography.headlineSmall
        )

        Text(
            text = stringResource(R.string.invite_token_format, inviteToken),
            style = MaterialTheme.typography.bodyMedium
        )

        Text(
            text = stringResource(R.string.device_id_format, deviceIdText),
            style = MaterialTheme.typography.bodySmall
        )

        uiState.errorMessage?.let { error ->
            Text(
                text = error.asString(),
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        OutlinedTextField(
            value = uiState.displayName,
            onValueChange = onDisplayNameChange,
            label = { Text(stringResource(R.string.your_name)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = onAcceptClick,
            enabled = !uiState.isLoading &&
                    !uiState.deviceId.isNullOrBlank() &&
                    uiState.displayName.isNotBlank(),
            modifier = Modifier.fillMaxWidth()
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator()
            } else {
                Text(stringResource(R.string.accept_invite))
            }
        }
    }
}