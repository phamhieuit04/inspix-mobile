package com.example.inspixmobile.data.dto.response

import kotlinx.serialization.Serializable

@Serializable
data class Response<T>(
    var code: Int? = null,
    var data: T? = null,
    var message: String? = null
)