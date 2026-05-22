package com.example.inspixmobile.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.inspixmobile.domain.contract.repository.ICollectionRepository
import com.example.inspixmobile.domain.model.Collection
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class DetailCollectionViewModel(
    private val collectionRepository: ICollectionRepository
) : ViewModel() {

    fun getCollectionByUuid(uuid: String): StateFlow<Collection?> {
        val collection = collectionRepository.getCollectionByUuid(uuid)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = null
            )

        return collection
    }
}