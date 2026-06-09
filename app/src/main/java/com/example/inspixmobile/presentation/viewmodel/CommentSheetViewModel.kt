package com.example.inspixmobile.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.inspixmobile.core.event.Event
import com.example.inspixmobile.core.event.EventBus
import com.example.inspixmobile.data.source.local.store.SessionStore
import com.example.inspixmobile.domain.contract.repository.ICommentRepository
import com.example.inspixmobile.domain.model.Comment
import com.example.inspixmobile.presentation.state.CommentSheetState
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CommentSheetViewModel(
    private val commentRepository: ICommentRepository,
    private val sessionStore: SessionStore
) : ViewModel() {

    private val _uiState = MutableStateFlow(CommentSheetState())
    val uiState = _uiState.asStateFlow()

    private var loadCommentsJob: Job? = null

    fun show(collectionUuid: String) {
        loadCommentsJob?.cancel()

        _uiState.update {
            it.copy(
                visible = true,
                comments = emptyList()
            )
        }

        loadCommentsJob = viewModelScope.launch {
            launch {
                try {
                    delay(500)
                    commentRepository.refreshComments(collectionUuid)
                } catch (e: Exception) {
                    Log.e("myapp", "Failed to refresh comments: ${e.message}")
                }
            }

            commentRepository.getCommentsByCollectionUuid(collectionUuid).collect { comments ->
                _uiState.update {
                    it.copy(
                        comments = comments
                    )
                }
            }
        }
    }

    fun hide() {
        loadCommentsJob?.cancel()

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

    fun requireSignIn() {
        viewModelScope.launch {
            val session = sessionStore.session.first()

            if (!session.isLoggedIn) {
                EventBus.emit(Event.RequireSignIn)
            }
        }
    }
}