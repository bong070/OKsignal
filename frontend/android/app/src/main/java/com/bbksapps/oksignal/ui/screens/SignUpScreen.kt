package com.bbksapps.oksignal.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import com.bbksapps.oksignal.R
import com.bbksapps.oksignal.ui.theme.Dimens

@Composable
fun SignUpScreen(
    onSignUpClick: (String, String, String) -> Unit,
    onGoogleSignUpClick: () -> Unit,
    onNavigateToLogin: () -> Unit,
    serverErrorMessage: String? = null
) {
    var email by remember { mutableStateOf("") }
    var displayName by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    var errorMessage by remember { mutableStateOf<String?>(null) }

    val errorEnterEmail = stringResource(R.string.error_enter_email)
    val errorEnterDisplayName = stringResource(R.string.error_enter_display_name)
    val errorEnterPassword = stringResource(R.string.error_enter_password)
    val errorEnterConfirmPassword = stringResource(R.string.error_enter_confirm_password)
    val errorPasswordMismatch = stringResource(R.string.error_password_mismatch)
    val errorPasswordTooShort = stringResource(R.string.error_password_too_short)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(
                horizontal = Dimens.ScreenHorizontalPadding,
                vertical = Dimens.ScreenVerticalPadding
            ),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text(
            text = stringResource(R.string.app_name),
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(Dimens.SpaceSm))

        Text(
            text = stringResource(R.string.signup_title),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(Dimens.SpaceXl))

        // Email
        OutlinedTextField(
            value = email,
            onValueChange = {
                email = it
                errorMessage = null
            },
            label = { Text(stringResource(R.string.email_label)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next
            )
        )

        Spacer(modifier = Modifier.height(Dimens.SpaceMd))

        // Display Name
        OutlinedTextField(
            value = displayName,
            onValueChange = {
                displayName = it
                errorMessage = null
            },
            label = { Text(stringResource(R.string.display_name_label)) },
            placeholder = { Text(stringResource(R.string.display_name_placeholder)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Next
            )
        )

        Spacer(modifier = Modifier.height(Dimens.SpaceMd))

        // Password
        OutlinedTextField(
            value = password,
            onValueChange = {
                password = it
                errorMessage = null
            },
            label = { Text(stringResource(R.string.password_label)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        imageVector = if (passwordVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                        contentDescription = stringResource(
                            if (passwordVisible) R.string.cd_hide_password else R.string.cd_show_password
                        )
                    )
                }
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Next
            )
        )

        Spacer(modifier = Modifier.height(Dimens.SpaceMd))

        // Confirm Password
        OutlinedTextField(
            value = confirmPassword,
            onValueChange = {
                confirmPassword = it
                errorMessage = null
            },
            label = { Text(stringResource(R.string.confirm_password_label)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                    Icon(
                        imageVector = if (confirmPasswordVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                        contentDescription = stringResource(
                            if (confirmPasswordVisible) R.string.cd_hide_password else R.string.cd_show_password
                        )
                    )
                }
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done
            )
        )

        Spacer(modifier = Modifier.height(Dimens.SpaceMd))

        val visibleErrorMessage = errorMessage ?: serverErrorMessage

        visibleErrorMessage?.let {
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(Dimens.SpaceSm))
        }

        Button(
            onClick = {
                val trimmedEmail = email.trim()
                val trimmedDisplayName = displayName.trim()

                errorMessage = when {
                    trimmedEmail.isBlank() -> errorEnterEmail
                    trimmedDisplayName.isBlank() -> errorEnterDisplayName
                    password.isBlank() -> errorEnterPassword
                    confirmPassword.isBlank() -> errorEnterConfirmPassword
                    password != confirmPassword -> errorPasswordMismatch
                    password.length < 6 -> errorPasswordTooShort
                    else -> null
                }

                if (errorMessage == null) {
                    onSignUpClick(trimmedEmail, trimmedDisplayName, password)
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.signup_button))
        }

        Spacer(modifier = Modifier.height(Dimens.SpaceMd))

        OutlinedButton(
            onClick = onGoogleSignUpClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.google_start))
        }

        Spacer(modifier = Modifier.height(Dimens.SpaceLg))

        TextButton(onClick = onNavigateToLogin) {
            Text(stringResource(R.string.go_to_login))
        }
    }
}