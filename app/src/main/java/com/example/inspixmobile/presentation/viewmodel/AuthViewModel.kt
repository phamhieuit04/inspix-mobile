package com.example.inspixmobile.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.inspixmobile.core.event.Event
import com.example.inspixmobile.core.event.EventBus
import com.example.inspixmobile.domain.contract.repository.IAuthRepository
import com.example.inspixmobile.domain.contract.repository.ICollectionRepository
import com.example.inspixmobile.domain.model.Collection
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class AuthViewModel(
    private val authRepository: IAuthRepository,
    private val collectionRepository: ICollectionRepository
) : ViewModel() {

    private val _collections = MutableStateFlow<List<Collection>>(emptyList())

    val collections = _collections.asStateFlow()

    init {
        getCachedCollections()
    }

    private fun getCachedCollections() {
        viewModelScope.launch {
            _collections.value =
                collectionRepository
                    .getCachedCollections()
                    .first()
                    .shuffled()
        }
    }


    fun signIn(email: String, password: String) {
        viewModelScope.launch {
            try {
                val user = authRepository.signIn(email, password)

                Log.i("myapp", "$user")
            } catch (e: Exception) {
                EventBus.emit(Event.NetworkError)

                Log.e("myapp", "${e.message}")
            }
        }
    }
}