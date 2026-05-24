package com.example.inspixmobile.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.inspixmobile.data.mapper.toDomain
import com.example.inspixmobile.domain.contract.repository.ICollectionRepository
import com.example.inspixmobile.domain.contract.repository.ICommentRepository
import com.example.inspixmobile.domain.model.Comment
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class DetailCollectionViewModel(
    private val collectionRepository: ICollectionRepository,
    private val commentRepository: ICommentRepository
) : ViewModel() {

    private val _comments = MutableStateFlow<List<Comment>>(emptyList())
    val comments: StateFlow<List<Comment>> = _comments.asStateFlow()

    fun getCommentsByCollectionUuid(collectionUuid: String) {
        viewModelScope.launch {
            try {
                val remoteComments = commentRepository.getCommentsByCollectionUuid(collectionUuid)
                val domainComments = remoteComments.data?.items?.map { it.toDomain() }.orEmpty()
                _comments.value = domainComments
            } catch (e: Exception) {
                _comments.value = emptyList()
                Log.i("myapp", "Error fetching comments: ${e.message}")
            }
        }
    }
}