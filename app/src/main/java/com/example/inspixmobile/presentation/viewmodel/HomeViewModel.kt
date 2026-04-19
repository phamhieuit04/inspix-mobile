package com.example.inspixmobile.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.inspixmobile.domain.contract.repository.ICollectionRepository
import com.example.inspixmobile.presentation.state.HomeUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HomeViewModel(
    private val collectionRepository: ICollectionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        observeCollections()
        refresh()
    }

    private fun observeCollections() {
        viewModelScope.launch {
            collectionRepository.getCollections().collect { data ->
                _uiState.update { current ->
                    current.copy(collections = data)
                }
            }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.update { current ->
                current.copy(
                    isRefreshing = true,
                    isInitialLoading = current.collections.isEmpty()
                )
            }

            collectionRepository.refreshCollections()

            _uiState.update { current ->
                current.copy(
                    isRefreshing = false,
                    isInitialLoading = false
                )
            }
        }
    }
}