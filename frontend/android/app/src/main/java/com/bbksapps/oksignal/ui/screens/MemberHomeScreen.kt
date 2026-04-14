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

@Composable
fun MemberHomeScreen(
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
            text = stringResource(R.string.member_ok),
            backgroundColor = SignalGreen,
            onClick = onOkClick
        )

        Spacer(modifier = Modifier.height(Dimens.MemberButtonSpacing))

        CircleButton(
            text = stringResource(R.string.member_help),
            backgroundColor = SignalRed,
            onClick = onHelpClick
        )
    }
}