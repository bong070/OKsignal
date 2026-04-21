package com.bbksapps.oksignal.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.bbksapps.oksignal.data.repository.InviteRepository
import com.bbksapps.oksignal.ui.theme.Dimens
import kotlinx.coroutines.launch
import retrofit2.HttpException
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.foundation.layout.fillMaxWidth

@Composable
fun InviteAcceptScreen(
    inviteToken: String,
    onAcceptSuccess: () -> Unit = {}
) {
    val repo = remember { InviteRepository() }
    val scope = rememberCoroutineScope()

    var resultText by remember { mutableStateOf("Invite token detected.") }
    var isLoading by remember { mutableStateOf(false) }

    val deviceId = "member-emulator-1"
    val deviceName = "Pixel 9 Pro Emulator"
    var displayName by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(Dimens.ScreenHorizontalPadding),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Invite",
            style = MaterialTheme.typography.headlineMedium
        )

        Text(
            text = inviteToken,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(top = Dimens.SpaceSm)
        )

        Spacer(modifier = Modifier.height(Dimens.SpaceLg))

        OutlinedTextField(
            value = displayName,
            onValueChange = { displayName = it },
            placeholder = {
                Text(
                    text = "이름을 입력하세요",
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                )
            },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = Dimens.SpaceLg)
        )

        Button(
            onClick = {
                scope.launch {
                    isLoading = true
                    resultText = "Connecting..."

                    try {
                        val response = repo.acceptInvite(
                            token = inviteToken,
                            deviceId = deviceId,
                            deviceName = deviceName,
                            displayName = displayName.trim()
                        )

                        if (response.success) {
                            resultText = "Connected successfully"
                            onAcceptSuccess()
                        } else {
                            resultText = response.error ?: "Invite accept failed"
                        }
                    } catch (e: HttpException) {
                        val errorBody = e.response()?.errorBody()?.string()
                        resultText = "HTTP ${e.code()}: ${errorBody ?: e.message()}"
                    } catch (e: Exception) {
                        resultText = e.message ?: "Unknown error"
                    } finally {
                        isLoading = false
                    }
                }
            },
            modifier = Modifier.padding(top = Dimens.SpaceLg),
            enabled = !isLoading && displayName.isNotBlank()
        ) {
            Text(if (isLoading) "Connecting..." else "Accept Invite")
        }

        Text(
            text = resultText,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(top = Dimens.SpaceLg)
        )
    }
}