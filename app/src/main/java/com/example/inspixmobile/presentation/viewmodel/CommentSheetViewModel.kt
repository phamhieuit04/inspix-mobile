package com.example.inspixmobile.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.inspixmobile.data.mapper.toDomain
import com.example.inspixmobile.domain.contract.repository.ICommentRepository
import com.example.inspixmobile.domain.model.Comment
import com.example.inspixmobile.presentation.state.CommentSheetState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CommentSheetViewModel(
    private val commentRepository: ICommentRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CommentSheetState())
    val uiState = _uiState.asStateFlow()

    fun show(collectionUuid: String) {
        viewModelScope.launch {
            try {
                val remoteComments = commentRepository.fetchCommentsByCollectionUuid(collectionUuid)
                val comments = remoteComments.data?.map { it.toDomain() }.orEmpty()

                _uiState.update { it.copy(visible = true, comments = comments) }
            } catch (e: Exception) {
                _uiState.update { it.copy(visible = true, comments = emptyList()) }

                Log.i("myapp", "Error fetching comments: ${e.message}")
            }
        }
    }

    fun hide() {
        _uiState.update {
            it.copy(
                visible = false,
                comments = emptyList(),
                inputText = "",
                replyingTo = null
            )
        }
    }

    fun updateInputText(text: String) {
        _uiState.update {
            it.copy(inputText = text)
        }
    }

    fun setReplyingTo(comment: Comment?) {
        _uiState.update {
            it.copy(replyingTo = comment)
        }
    }

    fun clearReplyingTo() {
        _uiState.update {
            it.copy(replyingTo = null)
        }
    }
}