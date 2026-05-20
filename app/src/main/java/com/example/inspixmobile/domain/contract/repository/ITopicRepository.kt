package com.example.inspixmobile.domain.contract.repository

import com.example.inspixmobile.domain.model.Topic
import kotlinx.coroutines.flow.Flow

interface ITopicRepository {
    fun getTopics(): Flow<List<Topic>>
}