package com.example.inspixmobile.presentation.viewmodel

import androidx.lifecycle.ViewModel
import com.example.inspixmobile.domain.model.Comment
import com.example.inspixmobile.presentation.state.CommentSheetState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class CommentSheetViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(CommentSheetState())
    val uiState = _uiState.asStateFlow()

    fun show(comments: List<Comment>) {
        _uiState.update { it.copy(visible = true, comments = comments) }
    }

    fun hide() {
        _uiState.update { it.copy(visible = false) }
    }
}