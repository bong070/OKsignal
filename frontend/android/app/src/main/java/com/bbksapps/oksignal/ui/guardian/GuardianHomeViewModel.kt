package com.bbksapps.oksignal.ui.guardian

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bbksapps.oksignal.data.local.repository.AppSessionRepository
import com.bbksapps.oksignal.data.repository.GuardianRepository
import com.bbksapps.oksignal.data.repository.InviteRepository
import com.bbksapps.oksignal.ui.common.UiMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.bbksapps.oksignal.ui.screens.GuardianMemberUiModel
import kotlinx.coroutines.flow.first

class GuardianHomeViewModel(
    private val appSessionRepository: AppSessionRepository,
    private val guardianRepository: GuardianRepository,
    private val inviteRepository: InviteRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(GuardianHomeUiState())
    val uiState: StateFlow<GuardianHomeUiState> = _uiState.asStateFlow()

    init {
        loadMembers()
    }

    private fun loadMembers() {
        viewModelScope.launch {
            val userId = appSessionRepository.appSessionState.first().user.userId

            if (userId.isNullOrBlank()) {
                _uiState.value = GuardianHomeUiState(
                    errorMessage = UiMessage.USER_NOT_READY
                )
                return@launch
            }

            _uiState.value = _uiState.value.copy(isLoading = true)

            try {
                val response = guardianRepository.getGuardianMembers(userId)

                if (response.success) {
                    _uiState.value = GuardianHomeUiState(
                        isLoading = false,
                        members = response.members.map { dto ->
                            GuardianMemberUiModel(
                                displayName = dto.member_display_name,
                                lastActive = dto.last_activity_at,
                                lastLocation = if (dto.last_known_lat != null && dto.last_known_lng != null) {
                                    "${dto.last_known_lng}, ${dto.last_known_lat}"
                                } else null,
                                isActive = dto.last_activity_at != null
                            )
                        }
                    )
                } else {
                    _uiState.value = GuardianHomeUiState(
                        isLoading = false,
                        errorMessage = UiMessage.FAILED_TO_LOAD_MEMBERS
                    )
                }
            } catch (e: Exception) {
                _uiState.value = GuardianHomeUiState(
                    isLoading = false,
                    errorMessage = UiMessage.UNKNOWN_ERROR
                )
            }
        }
    }

    fun createInvite() {
        viewModelScope.launch {
            val userId = appSessionRepository.appSessionState.first().user.userId
                ?: return@launch

            try {
                val response = inviteRepository.createInvite(userId)

                _uiState.value = _uiState.value.copy(
                    inviteDialogText = if (response.success) {
                        response.invite_link
                    } else {
                        null
                    },
                    inviteDialogIsError = !response.success,
                    errorMessage = if (response.success) null else UiMessage.UNKNOWN_ERROR
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    inviteDialogText = null,
                    inviteDialogIsError = true,
                    errorMessage = UiMessage.UNKNOWN_ERROR
                )
            }
        }
    }

    fun dismissInviteDialog() {
        _uiState.value = _uiState.value.copy(
            inviteDialogText = null
        )
    }

    fun refreshMembers() {
        loadMembers()
    }
}