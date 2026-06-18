package com.example.inspixmobile.core.util

import android.content.ContentValues
import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.compose.ui.graphics.Color
import androidx.palette.graphics.Palette
import coil3.ImageLoader
import coil3.request.ImageRequest
import coil3.request.allowHardware
import coil3.toBitmap
import com.example.inspixmobile.domain.model.Image
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsChannel
import io.ktor.http.contentLength
import io.ktor.utils.io.readAvailable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import kotlin.coroutines.coroutineContext

object ImageHelper {
    fun aspectRatio(width: Int?, height: Int?, fallback: Float = 3f / 4f): Float {
        val imageWidth = width?.toFloat() ?: return fallback
        val imageHeight = height?.toFloat() ?: return fallback
        if (imageWidth <= 0f || imageHeight <= 0f) return fallback
        return imageWidth / imageHeight
    }

    fun aspectRatio(context: Context, uri: Uri): Float? {
        return context.contentResolver.openInputStream(uri)?.use { input ->
            BitmapFactory.Options().apply {
                inJustDecodeBounds = true
                BitmapFactory.decodeStream(input, null, this)
            }.let { options ->
                if (options.outWidth > 0 && options.outHeight > 0) {
                    options.outWidth.toFloat() / options.outHeight.toFloat()
                } else {
                    null
                }
            }
        }
    }

    suspend fun getDominantColor(
        context: Context,
        imageUrl: String?
    ): Color = withContext(Dispatchers.IO) {

        val loader = ImageLoader(context)

        val request = ImageRequest.Builder(context)
            .data(imageUrl)
            .allowHardware(true)
            .build()

        val result = loader.execute(request)

        val bitmap = result.image?.toBitmap()
            ?: return@withContext Color.Gray

        val palette = Palette.from(bitmap)
            .resizeBitmapArea(10_000)
            .generate()

        val colorInt = palette.darkVibrantSwatch?.rgb
            ?: palette.vibrantSwatch?.rgb
            ?: palette.dominantSwatch?.rgb
            ?: android.graphics.Color.GRAY

        Color(colorInt)
    }
}


