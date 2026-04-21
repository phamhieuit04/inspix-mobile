package com.example.inspixmobile.domain.model

data class Image(
    val uuid: String? = null,
    val userUuid: String? = null,
    val collectionUuid: String? = null,
    val width: Int? = null,
    val height: Int? = null,

    val urlSmall: String? = null,
    val urlRegular: String? = null,
    val urlFull: String? = null,
    val downloadUrl: String? = null,
    val createdAt: String? = null,
    val createdAtHuman: String? = null,
    val updatedAt: String? = null,
    val updatedAtHuman: String? = null,
)
