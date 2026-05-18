package com.bbksapps.oksignal.data.local.model

data class AppSessionState(
    val device: DeviceState = DeviceState(),
    val user: UserState = UserState(),
    val session: SessionState = SessionState(),
    val relationship: RelationshipState = RelationshipState()
) {
    val isAuthenticated: Boolean
        get() {
            val hasUser = !user.userId.isNullOrBlank()

            val hasGuardianAuth =
                relationship.canActAsGuardian &&
                        session.isLoggedIn &&
                        !session.accessToken.isNullOrBlank()

            val hasMemberAuth =
                relationship.canActAsMember &&
                        session.isLoggedIn

            return hasUser && (hasGuardianAuth || hasMemberAuth)
        }

    val canSwitchModes: Boolean
        get() = relationship.canActAsGuardian && relationship.canActAsMember
}