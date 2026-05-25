package com.example.inspixmobile.domain.contract.repository

import com.example.inspixmobile.data.source.remote.dto.Response
import com.example.inspixmobile.data.source.remote.dto.TopicResponseDto
import com.example.inspixmobile.domain.model.Topic
import kotlinx.coroutines.flow.Flow

interface ITopicRepository {
    fun getTopics(): Flow<List<Topic>>

    suspend fun refreshTopics()

    suspend fun fetchRemoteTopics(): Response<List<TopicResponseDto>>
}