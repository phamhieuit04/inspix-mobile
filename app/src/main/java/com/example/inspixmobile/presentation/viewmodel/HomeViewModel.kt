package com.example.inspixmobile.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.inspixmobile.domain.contract.repository.ICollectionRepository
import com.example.inspixmobile.domain.model.Collection
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HomeViewModel(
    private val collectionRepository: ICollectionRepository
) : ViewModel() {
    private val _collections = MutableStateFlow<List<Collection>>(emptyList())
    val collections: StateFlow<List<Collection>> = _collections.asStateFlow()

    init {
        observeCollections()
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            collectionRepository.refreshCollections()
        }
    }

    private fun observeCollections() {
        viewModelScope.launch {
            collectionRepository.getCollections().collect { data ->
                _collections.value = data
            }
        }
    }
}