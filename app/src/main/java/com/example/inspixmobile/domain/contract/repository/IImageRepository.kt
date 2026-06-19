package com.example.inspixmobile.domain.contract.repository

import android.content.Context
import com.example.inspixmobile.domain.model.Image

interface IImageRepository {

    suspend fun download(
        context: Context,
        image: Image,
        onProgress: (Float) -> Unit = {}
    ): Result<Unit>

    suspend fun saveCapturedPhoto(
        context: Context,
        uri: String
    ): Result<Unit>
}