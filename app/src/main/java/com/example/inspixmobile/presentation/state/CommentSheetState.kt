package com.example.inspixmobile.presentation.state

import com.example.inspixmobile.domain.model.Comment

data class CommentSheetState(
    val visible: Boolean = false,
    val isLoading: Boolean = false,
    val comments: List<Comment> = emptyList(),
    val inputText: String = "",
    val replyingTo: Comment? = null
)