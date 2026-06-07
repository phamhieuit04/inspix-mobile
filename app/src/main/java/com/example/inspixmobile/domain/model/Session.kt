package com.example.inspixmobile.domain.model

data class Session(
    val accessToken: String? = null,
    val userUuid: String? = null
) {
    val isLoggedIn: Boolean
        get() = !accessToken.isNullOrBlank() &&
                !userUuid.isNullOrBlank()
}