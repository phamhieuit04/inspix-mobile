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
        image: Image,
        onProgress: (Float) -> Unit = {}
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
            val contentLength = response.headers["Content-Length"]?.toLongOrNull() ?: -1L

            val values = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
                put(MediaStore.Images.Media.MIME_TYPE, mimeType)
                put(
                    MediaStore.Images.Media.RELATIVE_PATH,
                    "${Environment.DIRECTORY_PICTURES}/Inspix"
                )
                put(MediaStore.Images.Media.IS_PENDING, 1)
            }

            val uri = context.contentResolver.insert(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                values
            ) ?: error("Cannot create media entry")

            try {
                val channel = response.bodyAsChannel()
                var bytesRead = 0L
                val buffer = ByteArray(DEFAULT_BUFFER_SIZE)

                context.contentResolver.openOutputStream(uri)
                    ?.use { output ->
                        while (!channel.isClosedForRead) {
                            if (!coroutineContext.isActive) error("Download cancelled")

                            val read = channel.readAvailable(buffer)
                            if (read <= 0) break

                            output.write(buffer, 0, read)
                            bytesRead += read

                            if (contentLength > 0) {
                                onProgress((bytesRead.toFloat() / contentLength).coerceIn(0f, 1f))
                            } else {
                                onProgress(-1f)
                            }
                        }
                        output.flush()
                    }
                    ?: error("Cannot open output stream")

                val update = ContentValues().apply {
                    put(MediaStore.Images.Media.IS_PENDING, 0)
                }
                context.contentResolver.update(uri, update, null, null)

                onProgress(1f)

            } catch (e: Exception) {
                context.contentResolver.delete(uri, null, null)
                throw e
            }
        }
    }
}


