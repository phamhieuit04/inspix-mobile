package com.example.inspixmobile.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.inspixmobile.core.event.Event
import com.example.inspixmobile.core.event.EventBus
import com.example.inspixmobile.domain.contract.repository.IAuthRepository
import com.example.inspixmobile.domain.contract.repository.ICollectionRepository
import com.example.inspixmobile.domain.model.Collection
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class AuthViewModel(
    private val authRepository: IAuthRepository,
    private val collectionRepository: ICollectionRepository
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _collections = MutableStateFlow<List<Collection>>(emptyList())

    val collections = _collections.asStateFlow()

    init {
        getCachedCollections()
    }

    private fun getCachedCollections() {
        viewModelScope.launch {
            collectionRepository
                .getCachedCollections()
                .collect { list ->
                    if (list.isNotEmpty()) {
                        _collections.value = list.shuffled()
                    }
                }
        }
    }


    fun signIn(email: String, password: String) {
        viewModelScope.launch {
            try {
                if (email.isNullOrBlank()) {
                    EventBus.emit(Event.ShowMessage("Vui lòng nhập email"))
                    return@launch
                }
                if (password.isNullOrBlank()) {
                    EventBus.emit(Event.ShowMessage("Vui lòng nhập mật khẩu"))
                    return@launch
                }

                _isLoading.value = true

                delay(500)
                val user = authRepository.signIn(email, password)

                _isLoading.value = false

                Log.i("myapp", "$user")
            } catch (e: Exception) {
                EventBus.emit(Event.NetworkError)

                Log.e("myapp", "${e.message}")
            }
        }
    }
}