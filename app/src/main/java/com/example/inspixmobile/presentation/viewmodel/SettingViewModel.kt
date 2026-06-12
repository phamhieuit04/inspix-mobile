package com.example.inspixmobile.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.inspixmobile.data.source.local.store.SettingStore
import com.example.inspixmobile.domain.contract.repository.IAuthRepository
import com.example.inspixmobile.presentation.component.NavigationBarStyle
import com.example.inspixmobile.presentation.screen.HomeLayoutStyle
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class SettingViewModel(
    private val settingStore: SettingStore,
    private val authRepository: IAuthRepository
) : ViewModel() {

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
            authRepository.logout()
        }
    }
}