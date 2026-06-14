package com.example.inspixmobile.core.util

import android.content.ContentValues
import android.content.Context
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
import io.ktor.http.contentLength
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object ImageHelper {
    fun aspectRatio(width: Int?, height: Int?, fallback: Float = 3f / 4f): Float {
        val imageWidth = width?.toFloat() ?: return fallback
        val imageHeight = height?.toFloat() ?: return fallback
        if (imageWidth <= 0f || imageHeight <= 0f) return fallback
        return imageWidth / imageHeight
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

    suspend fun download(
        context: Context,
        client: HttpClient,
        image: Image
    ): Result<Unit> {
        return runCatching {
            val imageUrl = image.urlFull ?: error("Url is null")
            val response = client.get(imageUrl)

            val mimeType = response.headers["Content-Type"]
                ?.substringBefore(";")
                ?: "image/jpeg"

            val extension = when (mimeType) {
                "image/jpeg" -> "jpg"
                "image/png" -> "png"
                "image/webp" -> "webp"
                "image/gif" -> "gif"
                else -> "jpg"
            }

            val fileName = "Inspix_${System.currentTimeMillis()}.$extension"

            val bytes: ByteArray = response.body()

            val values = ContentValues().apply {
                put(
                    MediaStore.Images.Media.DISPLAY_NAME,
                    fileName
                )
                put(
                    MediaStore.Images.Media.MIME_TYPE,
                    mimeType
                )
                put(
                    MediaStore.Images.Media.RELATIVE_PATH,
                    "${Environment.DIRECTORY_PICTURES}/Inspix"
                )
            }

            val uri = context.contentResolver.insert(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                values
            ) ?: error("Cannot create media entry")

            context.contentResolver
                .openOutputStream(uri)
                ?.use { output ->
                    output.write(bytes)
                    output.flush()
                }
                ?: error("Cannot open output stream")
        }
    }
}


