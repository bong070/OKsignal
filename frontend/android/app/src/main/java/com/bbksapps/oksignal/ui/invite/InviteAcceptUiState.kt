package com.bbksapps.oksignal.ui.invite

import com.bbksapps.oksignal.ui.common.UiMessage

data class InviteAcceptUiState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val errorMessage: UiMessage? = null,
    val deviceId: String? = null,
    val displayName: String = ""
)