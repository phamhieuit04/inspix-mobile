package com.example.inspixmobile.data.source.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class Response<T>(
    var code: Int? = null,
    var data: T? = null,
    var message: String? = null
)
