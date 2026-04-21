package com.bbksapps.oksignal.ui.invite

data class InviteAcceptUiState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val errorMessage: String? = null,
    val deviceId: String? = null
)