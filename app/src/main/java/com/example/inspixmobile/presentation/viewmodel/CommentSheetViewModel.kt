package com.example.inspixmobile.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.inspixmobile.data.mapper.toDomain
import com.example.inspixmobile.domain.contract.repository.ICommentRepository
import com.example.inspixmobile.domain.model.Comment
import com.example.inspixmobile.presentation.state.CommentSheetState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CommentSheetViewModel(
    private val commentRepository: ICommentRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(CommentSheetState())
    val uiState = _uiState.asStateFlow()

    fun show(comments: List<Comment>) {
        _uiState.update { it.copy(visible = true, comments = comments) }
    }

    fun hide() {
        _uiState.update { it.copy(visible = false) }
    }

    private val _comments = MutableStateFlow<List<Comment>>(emptyList())
    val comments: StateFlow<List<Comment>> = _comments.asStateFlow()

    fun getCommentsByCollectionUuid(collectionUuid: String) {
        viewModelScope.launch {
            try {
                val remoteComments = commentRepository.getCommentsByCollectionUuid(collectionUuid)
                val domainComments = remoteComments.data?.items?.map { it.toDomain() }.orEmpty()
                _comments.value = domainComments
            } catch (e: Exception) {
                _comments.value = emptyList()
                Log.i("myapp", "Error fetching comments: ${e.message}")
            }
        }
    }
}