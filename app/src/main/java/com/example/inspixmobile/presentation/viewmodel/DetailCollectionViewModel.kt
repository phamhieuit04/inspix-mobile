package com.example.inspixmobile.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.inspixmobile.domain.contract.repository.ICollectionRepository
import com.example.inspixmobile.domain.contract.repository.ICommentRepository
import kotlinx.coroutines.launch

class DetailCollectionViewModel(
    private val collectionRepository: ICollectionRepository,
    private val commentRepository: ICommentRepository
) : ViewModel() {

    fun getCommentsByCollectionUuid(collectionUuid: String) {
        viewModelScope.launch {
            try {
                val response = commentRepository.getCommentsByCollectionUuid(collectionUuid)
                Log.i("myapp", "Comments for collection $collectionUuid: ${response.data}")

            } catch (e: Exception) {
                Log.i("myapp", "Error fetching comments: ${e.message}")
            }
        }
    }

}