package com.example.inspixmobile.presentation.state

import com.example.inspixmobile.domain.model.Comment

data class CommentSheetState(
    val isLoading: Boolean = false,
    val visible: Boolean = false,
    val collectionUuid: String? = null,
    val comments: List<Comment> = emptyList(),
    val inputText: String = "",
    val replyingTo: Comment? = null,
    val scrollToCommentId: Long? = null
)