package com.example.inspixmobile.presentation.state

sealed interface InteractionState {
    data object Unauthorized : InteractionState
    data object Network : InteractionState
    data object Success : InteractionState
    data class Unknown(val message: String?) : InteractionState
}