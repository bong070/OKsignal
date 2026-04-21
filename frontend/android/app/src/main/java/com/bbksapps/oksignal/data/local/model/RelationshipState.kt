package com.bbksapps.oksignal.data.local.model

data class RelationshipState(
    val canActAsGuardian: Boolean = false,
    val canActAsMember: Boolean = false,
    val selectedMode: AppMode = AppMode.MEMBER,
    val selectedMemberUserId: String? = null,
    val selectedGuardianUserId: String? = null,
    val selectedGroupId: String? = null
)