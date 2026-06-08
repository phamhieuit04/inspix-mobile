package com.example.inspixmobile.presentation.state

data class CollectionInteractionState(
    val isLiked: Boolean,
    val totalLikes: Int,
    val totalComments: Int
)