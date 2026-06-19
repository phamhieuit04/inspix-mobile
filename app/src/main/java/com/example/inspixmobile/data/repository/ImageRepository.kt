package com.example.inspixmobile.data.repository

import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import androidx.core.net.toUri
import com.example.inspixmobile.domain.contract.repository.IImageRepository
import com.example.inspixmobile.domain.model.Image
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsChannel
import io.ktor.utils.io.readAvailable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import kotlin.coroutines.coroutineContext
import kotlin.text.substringBefore
import kotlin.text.toLongOrNull

class ImageRepository(
    private val client: HttpClient
) : IImageRepository {

    override suspend fun download(
        context: Context,
        image: Image,
        onProgress: (Float) -> Unit
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
                                onProgress(
                                    (bytesRead.toFloat() / contentLength).coerceIn(
                                        0f,
                                        1f
                                    )
                                )
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

    override suspend fun saveCapturedPhoto(
        context: Context,
        uri: String
    ): Result<Unit> = runCatching {

        val values = ContentValues().apply {
            put(
                MediaStore.Images.Media.DISPLAY_NAME,
                "Inspix_${System.currentTimeMillis()}.jpg"
            )
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            put(
                MediaStore.Images.Media.RELATIVE_PATH,
                "${Environment.DIRECTORY_PICTURES}/Inspix"
            )
        }

        val destinationUri = context.contentResolver.insert(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            values
        ) ?: error("Cannot create media entry")

        context.contentResolver.openInputStream(uri.toUri())!!.use { input ->
            context.contentResolver.openOutputStream(destinationUri)!!.use { output ->
                input.copyTo(output)
            }
        }
    }

    suspend fun getLatestImageUri(
        context: Context,
        includeScreenshots: Boolean = true
    ): Uri? = withContext(Dispatchers.IO) {

        val projection = arrayOf(
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.BUCKET_DISPLAY_NAME
        )

        val selection: String?
        val selectionArgs: Array<String>?

        if (includeScreenshots) {
            selection = null
            selectionArgs = null
        } else {
            selection = "${MediaStore.Images.Media.BUCKET_DISPLAY_NAME} NOT LIKE ?"
            selectionArgs = arrayOf("%Screenshots%")
        }

        val sortOrder = "${MediaStore.Images.Media.DATE_TAKEN} DESC"

        context.contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            projection,
            selection,
            selectionArgs,
            sortOrder
        )?.use { cursor ->

            if (!cursor.moveToFirst()) {
                return@withContext null
            }

            val idColumn = cursor.getColumnIndexOrThrow(
                MediaStore.Images.Media._ID
            )

            val imageId = cursor.getLong(idColumn)

            return@withContext ContentUris.withAppendedId(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                imageId
            )
        }

        null
    }
}