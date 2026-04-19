package com.example.inspixmobile.domain.contract.repository

import com.example.inspixmobile.data.dto.response.CollectionResponseDto

interface ICollectionRepository {
    suspend fun getCollections(): List<CollectionResponseDto>?
}