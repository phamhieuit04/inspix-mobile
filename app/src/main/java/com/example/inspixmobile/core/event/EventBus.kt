package com.example.inspixmobile.core.event

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow

sealed interface Event {
    object RequireSignIn : Event

    object NetworkError : Event

    object SignOutSuccess : Event

    object SignInSuccess : Event
}

object EventBus {
    private val _events = Channel<Event>()
    val events = _events.receiveAsFlow()

    suspend fun emit(event: Event) {
        _events.send(event)
    }
}