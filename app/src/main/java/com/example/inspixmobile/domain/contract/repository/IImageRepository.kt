package com.example.inspixmobile.domain.contract.repository

import android.content.Context
import com.example.inspixmobile.domain.model.Image

interface IImageRepository {

    suspend fun download(
        context: Context,
        source: String,
        onProgress: (Float) -> Unit
    ): Result<Unit>
}