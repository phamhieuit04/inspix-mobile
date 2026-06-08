package com.example.inspixmobile.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.inspixmobile.domain.contract.repository.IUserRepository
import com.example.inspixmobile.domain.model.User
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn

class ProfileViewModel(
    private val userRepository: IUserRepository
) : ViewModel() {

    private val _userUuid = MutableStateFlow<String?>(null)

    fun setUserUuid(uuid: String) {
        if (_userUuid.value == uuid) return

        _userUuid.value = uuid
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val user = _userUuid
        .filterNotNull()
        .flatMapLatest { uuid ->
            userRepository.findUser(uuid)
        }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            null
        )
}