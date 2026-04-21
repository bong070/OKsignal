package com.bbksapps.oksignal.data.remote

data class CreateInviteRequest(
    val guardian_user_id: String
)

data class CreateInviteResponse(
    val success: Boolean,
    val token: String? = null,
    val invite_link: String? = null,
    val error: String? = null,
    val reused: Boolean? = null
)

data class AcceptInviteRequest(
    val token: String,
    val device_id: String,
    val fcm_token: String? = null,
    val display_name: String? = null,
    val email: String? = null,
    val phone_number: String? = null,
    val device_name: String? = null
)

data class AcceptInviteResponse(
    val success: Boolean,
    val error: String? = null,
    val link_created: Boolean? = null,
    val member_created: Boolean? = null
)

data class GuardianMembersResponse(
    val success: Boolean,
    val guardian_user_id: String,
    val members: List<GuardianMemberDto> = emptyList(),
    val error: String? = null
)

data class GuardianMemberDto(
    val link_id: String,
    val link_status: String,
    val is_primary_visible: Int,
    val linked_at: String,
    val member_user_id: String,
    val member_display_name: String? = null,
    val member_email: String? = null,
    val member_phone_number: String? = null,
    val member_status: String? = null,
    val device_id: String? = null,
    val device_name: String? = null,
    val last_ping_at: String? = null,
    val last_activity_at: String? = null,
    val last_known_lat: Double? = null,
    val last_known_lng: Double? = null,
    val last_known_location_at: String? = null
)