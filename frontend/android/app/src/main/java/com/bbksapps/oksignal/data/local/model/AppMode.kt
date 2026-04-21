package com.bbksapps.oksignal.data.local.model

enum class AppMode {
    GUARDIAN,
    MEMBER,
    GROUP;

    companion object {
        fun fromValue(value: String?): AppMode {
            return when (value?.uppercase()) {
                "GUARDIAN" -> GUARDIAN
                "GROUP" -> GROUP
                else -> MEMBER
            }
        }
    }
}