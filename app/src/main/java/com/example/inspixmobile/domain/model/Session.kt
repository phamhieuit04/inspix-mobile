package com.example.inspixmobile.domain.model

data class Session(
    val accessToken: String?,
    val userUuid: String?
) {
    val isLoggedIn: Boolean
        get() = !accessToken.isNullOrBlank() &&
                !userUuid.isNullOrBlank()
}