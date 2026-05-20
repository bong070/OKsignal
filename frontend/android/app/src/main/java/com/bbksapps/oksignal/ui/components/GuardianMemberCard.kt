package com.bbksapps.oksignal.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.bbksapps.oksignal.R
import com.bbksapps.oksignal.ui.theme.CardBorderColor
import com.bbksapps.oksignal.ui.theme.Dimens
import com.bbksapps.oksignal.ui.theme.SignalGreen
import com.bbksapps.oksignal.ui.theme.SignalRed
import com.bbksapps.oksignal.ui.common.calculateTimeAgo
import com.bbksapps.oksignal.ui.common.asString

@Composable
fun GuardianMemberCard(
    displayName: String?,
    lastActive: String?,
    lastLocation: String?,
    isActive: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = if (isActive) SignalGreen else SignalRed
    val lastActiveText =
        calculateTimeAgo(lastActive).asString()
            ?: stringResource(R.string.no_activity)

    Card(
        modifier = Modifier
            .fillMaxSize()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(Dimens.GuardianCardRadius),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = Dimens.CardElevation
        ),

        border = BorderStroke(
            width = Dimens.CardBorderWidth,
            color = CardBorderColor
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(Dimens.SpaceMd),
            verticalArrangement = Arrangement.Top
        ) {
            Text(
                text = displayName ?: stringResource(R.string.unknown_member),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimary
            )

            Text(
                text = stringResource(R.string.member_last_active),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.padding(top = Dimens.SpaceLg)
            )

            Text(
                text = lastActiveText,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.padding(top = Dimens.SpaceXs)
            )

            if (!isActive && !lastLocation.isNullOrBlank()) {
                Text(
                    text = stringResource(R.string.member_last_location),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.padding(top = Dimens.SpaceLg)
                )

                Text(
                    text = lastLocation,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.padding(top = Dimens.SpaceXs)
                )
            }
        }
    }
}