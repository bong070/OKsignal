package com.bbksapps.oksignal.ui.guardian

import com.bbksapps.oksignal.ui.screens.GuardianMemberUiModel

data class GuardianHomeUiState(
    val isLoading: Boolean = false,
    val members: List<GuardianMemberUiModel> = emptyList(),
    val errorMessage: String? = null,
    val inviteDialogText: String? = null,
    val inviteDialogIsError: Boolean = false
)