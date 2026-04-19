package com.example.inspixmobile.domain.contract.repository

import com.example.inspixmobile.domain.model.Collection
import kotlinx.coroutines.flow.Flow

interface ICollectionRepository {
    fun getCollections(): Flow<List<Collection>>
    suspend fun refreshCollections()
}