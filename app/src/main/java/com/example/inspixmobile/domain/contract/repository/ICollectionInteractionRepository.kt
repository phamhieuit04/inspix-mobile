package com.example.inspixmobile.domain.contract.repository

import com.example.inspixmobile.domain.model.Collection
import com.example.inspixmobile.presentation.state.CollectionInteractionState
import kotlinx.coroutines.flow.StateFlow

interface ICollectionInteractionRepository {
    val interactions: StateFlow<Map<String, CollectionInteractionState>>

    fun seed(collection: Collection)

    suspend fun toggleLike(collectionUuid: String)

    fun clear()
}