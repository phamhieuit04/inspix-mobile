package com.example.inspixmobile.data.dto.response

import kotlinx.serialization.Serializable

@Serializable
data class ImageResponseDto(
    val uuid: String? = null,
    val color: String? = null,
    val urls: ImageUrlsResponseDto? = null,
    val url_small: String? = null,
    val url_regular: String? = null,
    val url_full: String? = null,
    val user_id: Long? = null,
    val collection_id: Long? = null,
    val download_url: String? = null,
    val created_at: String? = null,
    val updated_at: String? = null,
    val created_at_human: String? = null,
    val updated_at_human: String? = null
)

@Serializable
data class ImageUrlsResponseDto(
    val small: String? = null,
    val regular: String? = null,
    val full: String? = null,
    val download: String? = null
)
