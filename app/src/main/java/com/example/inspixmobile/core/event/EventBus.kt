package com.example.inspixmobile.core.event

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow

sealed interface Event {
    data object RequireSignIn : Event
}

object EventBus {
    private val _events = Channel<Event>()
    val events = _events.receiveAsFlow()

    suspend fun emit(event: Event) {
        _events.send(event)
    }
}