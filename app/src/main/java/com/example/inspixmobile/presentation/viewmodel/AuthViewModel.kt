package com.example.inspixmobile.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.inspixmobile.domain.contract.repository.IAuthRepository
import kotlinx.coroutines.launch

class AuthViewModel(
    private val authRepository: IAuthRepository
) : ViewModel() {

    fun signIn(email: String, password: String) {
        viewModelScope.launch {
            try {
                val user = authRepository.signIn(email, password)

                Log.i("myapp", "$user")
            } catch (e: Exception) {
                Log.e("myapp", "${e.message}")
            }
        }
    }
}