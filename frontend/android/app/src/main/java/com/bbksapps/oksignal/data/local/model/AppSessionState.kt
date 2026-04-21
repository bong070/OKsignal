package com.bbksapps.oksignal.data.local.model

data class AppSessionState(
    val device: DeviceState = DeviceState(),
    val user: UserState = UserState(),
    val session: SessionState = SessionState(),
    val relationship: RelationshipState = RelationshipState()
) {
    val isAuthenticated: Boolean
        get() = session.isLoggedIn &&
                !session.accessToken.isNullOrBlank() &&
                !user.userId.isNullOrBlank()

    val canSwitchModes: Boolean
        get() = relationship.canActAsGuardian && relationship.canActAsMember
}