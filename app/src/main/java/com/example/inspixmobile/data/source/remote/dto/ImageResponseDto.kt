package com.example.inspixmobile.data.source.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class ImageResponseDto(
    val uuid: String? = null,
    val color: String? = null,
    val width: Int? = null,
    val height: Int? = null,
    val url_small: String? = null,
    val url_regular: String? = null,
    val url_full: String? = null,
    val user_uuid: String? = null,
    val collection_uuid: String? = null,
    val download_url: String? = null,
    val created_at: String? = null,
    val updated_at: String? = null,
    val created_at_human: String? = null,
    val updated_at_human: String? = null
)


