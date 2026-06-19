package com.example.inspixmobile.presentation.viewmodel

import androidx.compose.runtime.collectAsState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.inspixmobile.data.source.local.store.SettingStore
import com.example.inspixmobile.domain.contract.repository.IAuthRepository
import com.example.inspixmobile.presentation.component.NavigationBarStyle
import com.example.inspixmobile.presentation.screen.HomeLayoutStyle
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class SettingViewModel(
    private val settingStore: SettingStore,
    private val authRepository: IAuthRepository
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    fun updateHomeLayout(layout: HomeLayoutStyle) {
        viewModelScope.launch {
            settingStore.saveSetting(
                homeLayout = layout,
                navbarLayout = settingStore.setting.first().navbarLayout
            )
        }
    }

    fun updateNavbarLayout(layout: NavigationBarStyle) {
        viewModelScope.launch {
            settingStore.saveSetting(
                homeLayout = settingStore.setting.first().homeLayout,
                navbarLayout = layout
            )
        }
    }

    fun logout() {
        viewModelScope.launch {
            _isLoading.value = true

            delay(500)
            authRepository.logout()

            _isLoading.value = false
        }
    }
}