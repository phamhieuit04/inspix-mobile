package com.example.inspixmobile.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.inspixmobile.data.mapper.toDomain
import com.example.inspixmobile.domain.contract.repository.ICollectionRepository
import com.example.inspixmobile.domain.contract.repository.ICommentRepository
import com.example.inspixmobile.domain.model.Comment
import kotlinx.coroutines.launch

class DetailCollectionViewModel(
    private val collectionRepository: ICollectionRepository,
    private val commentRepository: ICommentRepository
) : ViewModel() {

    fun getCommentsByCollectionUuid(collectionUuid: String): List<Comment> {
        viewModelScope.launch {
            try {
                val remoteComments = commentRepository.getCommentsByCollectionUuid(collectionUuid)
                val domainComments = remoteComments.data?.items?.map { it.toDomain() }

            } catch (e: Exception) {
                Log.i("myapp", "Error fetching comments: ${e.message}")
            }
        }
    }
}