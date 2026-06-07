package com.example.inspixmobile.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.inspixmobile.data.source.local.store.SessionStore
import com.example.inspixmobile.data.source.local.store.SettingStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class SplashViewModel(
    private val sessionStore: SessionStore,
    private val settingStore: SettingStore
) : ViewModel() {

    private val _isReady = MutableStateFlow(false)
    val isReady = _isReady.asStateFlow()

    init {
        viewModelScope.launch {
            sessionStore.session.first()
            settingStore.setting.first()

            _isReady.value = true
        }
    }
}