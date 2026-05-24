package com.bbksapps.oksignal.ui.member

import com.bbksapps.oksignal.ui.common.UiMessage

data class MemberHomeUiState(
    val isCheckingIn: Boolean = false,
    val lastCheckInSuccess: Boolean = false,
    val isSendingHelp: Boolean = false,
    val needHelpSuccess: Boolean = false,
    val errorMessage: UiMessage? = null
)