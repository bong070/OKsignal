package com.bbksapps.oksignal.ui.guardian

import com.bbksapps.oksignal.ui.common.UiMessage
import com.bbksapps.oksignal.ui.screens.GuardianMemberUiModel

data class GuardianHomeUiState(
    val isLoading: Boolean = false,
    val members: List<GuardianMemberUiModel> = emptyList(),
    val errorMessage: UiMessage? = null,
    val inviteDialogText: String? = null,
    val inviteDialogIsError: Boolean = false,
    val refreshedAtMillis: Long = System.currentTimeMillis()
)