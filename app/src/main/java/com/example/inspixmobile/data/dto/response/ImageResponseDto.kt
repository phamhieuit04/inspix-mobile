package com.example.inspixmobile.data.dto.response

import java.time.LocalDateTime
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable

@Serializable
data class ImageResponseDto(
    val uuid: String? = null,
    val color: String? = null,
    val url_small: String? = null,
    val url_regular: String? = null,
    val url_full: String? = null,
    val user_id: Long? = null,
    val collection_id: Long? = null,
    val download_url: String? = null,
    @Contextual
    val created_at: LocalDateTime? = null,
    @Contextual
    val updated_at: LocalDateTime? = null
)

