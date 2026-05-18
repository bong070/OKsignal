package com.bbksapps.oksignal.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.bbksapps.oksignal.R
import com.bbksapps.oksignal.ui.components.CircleButton
import com.bbksapps.oksignal.ui.theme.Dimens
import com.bbksapps.oksignal.ui.theme.SignalGreen
import com.bbksapps.oksignal.ui.theme.SignalRed
import androidx.compose.material3.Text
import com.bbksapps.oksignal.ui.common.asString
import com.bbksapps.oksignal.ui.member.MemberHomeUiState

@Composable
fun MemberHomeScreen(
    uiState: MemberHomeUiState,
    onOkClick: () -> Unit = {},
    onHelpClick: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircleButton(
            text = if (uiState.isCheckingIn) {
                stringResource(R.string.checking_in)
            } else {
                stringResource(R.string.member_ok)
            },
            backgroundColor = SignalGreen,
            onClick = onOkClick
        )

        Spacer(modifier = Modifier.height(Dimens.MemberButtonSpacing))

        uiState.errorMessage?.let { error ->
            Spacer(modifier = Modifier.height(Dimens.SpaceMd))
            Text(
                text = error.asString(),
                color = MaterialTheme.colorScheme.error
            )
        }

        if (uiState.lastCheckInSuccess) {
            Spacer(modifier = Modifier.height(Dimens.SpaceMd))
            Text(
                text = stringResource(R.string.check_in_success),
                color = SignalGreen
            )
        }

        CircleButton(
            text = stringResource(R.string.member_help),
            backgroundColor = SignalRed,
            onClick = onHelpClick
        )
    }
}