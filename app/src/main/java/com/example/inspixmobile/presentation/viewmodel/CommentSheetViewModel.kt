package com.example.inspixmobile.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.inspixmobile.core.event.Event
import com.example.inspixmobile.core.event.EventBus
import com.example.inspixmobile.data.mapper.toDomain
import com.example.inspixmobile.data.source.local.store.SessionStore
import com.example.inspixmobile.domain.contract.repository.ICollectionInteractionRepository
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
import kotlin.coroutines.cancellation.CancellationException

class CommentSheetViewModel(
    private val commentRepository: ICommentRepository,
    private val collectionInteractionRepository: ICollectionInteractionRepository,
    private val sessionStore: SessionStore
) : ViewModel() {

    private val _uiState = MutableStateFlow(CommentSheetState())
    val uiState = _uiState.asStateFlow()

    private var loadCommentsJob: Job? = null

    fun show(collectionUuid: String) {
        loadCommentsJob?.cancel()

        _uiState.update {
            it.copy(
                isLoading = true,
                visible = true,
                collectionUuid = collectionUuid,
                comments = emptyList()
            )
        }

        loadCommentsJob = viewModelScope.launch {
            launch {
                commentRepository
                    .getCommentsByCollectionUuid(collectionUuid)
                    .collect { comments ->
                        _uiState.update {
                            it.copy(comments = comments)
                        }
                    }
            }

            try {
                commentRepository.refreshComments(collectionUuid)
            } catch (e: Exception) {
                Log.e("myapp", "Failed to refresh comments", e)
            } finally {
                _uiState.update {
                    it.copy(isLoading = false)
                }
            }
        }
    }

    fun hide() {
        loadCommentsJob?.cancel()

        _uiState.update {
            it.copy(
                isLoading = false,
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

    fun setReplyingTo(comment: Comment) {
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

    fun postComment(context: String) {
        val collectionUuid = uiState.value.collectionUuid ?: return
        val parentId = uiState.value.replyingTo?.id

        viewModelScope.launch {
            _uiState.update {
                it.copy(inputText = "", replyingTo = null)
            }

            val result = collectionInteractionRepository.postComment(
                collectionUuid = collectionUuid,
                context = context,
                parentId = parentId
            )

            Log.i("myapp", "${result.data}}")

            result.data?.id?.let { newId ->
                _uiState.update { it.copy(scrollToCommentId = newId) }
            }
        }
    }

    fun clearScrollTarget() {
        _uiState.update { it.copy(scrollToCommentId = null) }
    }
}