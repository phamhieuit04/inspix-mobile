package com.example.inspixmobile.presentation.state

import com.example.inspixmobile.domain.model.Collection

data class HomeUiState(
    val collections: List<Collection> = emptyList(),
    val isRefreshing: Boolean = false,
    val isInitialLoading: Boolean = true
)
