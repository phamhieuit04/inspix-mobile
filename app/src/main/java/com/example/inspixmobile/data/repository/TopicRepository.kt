package com.example.inspixmobile.data.repository

import android.util.Log
import com.example.inspixmobile.data.mapper.toDomain
import com.example.inspixmobile.data.mapper.toEntity
import com.example.inspixmobile.data.source.local.dao.TopicDao
import com.example.inspixmobile.data.source.remote.dto.CollectionResponseDto
import com.example.inspixmobile.data.source.remote.dto.Response
import com.example.inspixmobile.data.source.remote.dto.TopicResponseDto
import com.example.inspixmobile.domain.contract.repository.ITopicRepository
import com.example.inspixmobile.domain.model.Topic
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.statement.bodyAsText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okhttp3.Dispatcher

class TopicRepository(
    private val client: HttpClient,
    private val json: Json,
    private val topicDao: TopicDao
) : ITopicRepository {

    override fun getTopics(): Flow<List<Topic>> = flow {
        val remoteTopics = fetchRemoteTopics().data?.map { it.toDomain() } ?: emptyList()
        topicDao.insertAll(
            remoteTopics.map { it.toEntity() }
        )

        emitAll(topicDao.getAll().map { list -> list.map { it.toDomain() } })
    }

    private suspend fun fetchRemoteTopics(): Response<List<TopicResponseDto>> {
        val body = client.get("v1/topics").bodyAsText()

        return json.decodeFromString(body)
    }
}