package com.example.inspixmobile.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.inspixmobile.domain.contract.repository.ICollectionRepository
import kotlinx.coroutines.launch

class HomeViewModel(
    private val collectionRepository: ICollectionRepository
) : ViewModel() {

    init {
        fetchCollections()
    }

    fun fetchCollections() {
        viewModelScope.launch {
            val collections = collectionRepository.getCollections()
        }
    }
}