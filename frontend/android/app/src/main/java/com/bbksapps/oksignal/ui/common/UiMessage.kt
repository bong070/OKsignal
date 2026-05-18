package com.bbksapps.oksignal.ui.common

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.bbksapps.oksignal.R

enum class UiMessage {
    USER_NOT_READY,
    DEVICE_ID_NOT_READY,
    DEVICE_ID_NOT_READY_TRY_AGAIN,
    LOGIN_REQUIRED_FIELDS,
    SIGNUP_REQUIRED_FIELDS,
    ENTER_NAME,
    LOGIN_FAILED,
    SIGNUP_FAILED,
    ACCEPT_INVITE_FAILED,
    FAILED_TO_LOAD_MEMBERS,
    UNKNOWN_ERROR,
    ENTER_EMAIL,
    ENTER_PASSWORD,
    ENTER_DISPLAY_NAME,
    ENTER_CONFIRM_PASSWORD,
    PASSWORD_MISMATCH,
    PASSWORD_TOO_SHORT
}

@Composable
fun UiMessage.asString(): String {
    return when (this) {
        UiMessage.USER_NOT_READY -> stringResource(R.string.error_user_not_ready)
        UiMessage.DEVICE_ID_NOT_READY -> stringResource(R.string.error_device_id_not_ready)
        UiMessage.DEVICE_ID_NOT_READY_TRY_AGAIN -> stringResource(R.string.error_device_id_not_ready_try_again)
        UiMessage.LOGIN_REQUIRED_FIELDS -> stringResource(R.string.error_login_required_fields)
        UiMessage.SIGNUP_REQUIRED_FIELDS -> stringResource(R.string.error_signup_required_fields)
        UiMessage.ENTER_NAME -> stringResource(R.string.error_enter_name)
        UiMessage.LOGIN_FAILED -> stringResource(R.string.login_failed)
        UiMessage.SIGNUP_FAILED -> stringResource(R.string.signup_failed)
        UiMessage.ACCEPT_INVITE_FAILED -> stringResource(R.string.accept_invite_failed)
        UiMessage.FAILED_TO_LOAD_MEMBERS -> stringResource(R.string.error_failed_to_load_members)
        UiMessage.UNKNOWN_ERROR -> stringResource(R.string.error_unknown)
        UiMessage.ENTER_EMAIL -> stringResource(R.string.error_enter_email)
        UiMessage.ENTER_PASSWORD -> stringResource(R.string.error_enter_password)
        UiMessage.ENTER_DISPLAY_NAME -> stringResource(R.string.error_enter_display_name)
        UiMessage.ENTER_CONFIRM_PASSWORD -> stringResource(R.string.error_enter_confirm_password)
        UiMessage.PASSWORD_MISMATCH -> stringResource(R.string.error_password_mismatch)
        UiMessage.PASSWORD_TOO_SHORT -> stringResource(R.string.error_password_too_short)
    }
}