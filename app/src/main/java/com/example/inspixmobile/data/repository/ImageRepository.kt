package com.example.inspixmobile.data.repository

import android.content.Context
import com.example.inspixmobile.core.util.ImageHelper
import com.example.inspixmobile.domain.contract.repository.IImageRepository
import com.example.inspixmobile.domain.model.Image
import io.ktor.client.HttpClient

class ImageRepository(
    private val client: HttpClient
) : IImageRepository {

    override suspend fun download(context: Context, image: Image) {
        ImageHelper.download(context, client, image)
    }
}