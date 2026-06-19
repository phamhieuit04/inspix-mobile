package com.example.inspixmobile.core.util

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import androidx.exifinterface.media.ExifInterface
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.compose.ui.graphics.Color
import androidx.palette.graphics.Palette
import coil3.ImageLoader
import coil3.imageLoader
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
import androidx.core.graphics.scale

object ImageHelper {
    fun aspectRatio(width: Int?, height: Int?, fallback: Float = 3f / 4f): Float {
        val imageWidth = width?.toFloat() ?: return fallback
        val imageHeight = height?.toFloat() ?: return fallback
        if (imageWidth <= 0f || imageHeight <= 0f) return fallback
        return imageWidth / imageHeight
    }

    fun getImageSize(
        context: Context,
        uri: Uri
    ): Pair<Int, Int>? {
        return try {
            context.contentResolver.openInputStream(uri)?.use { input ->

                val options = BitmapFactory.Options().apply {
                    inJustDecodeBounds = true
                }

                BitmapFactory.decodeStream(input, null, options)

                var width = options.outWidth
                var height = options.outHeight

                context.contentResolver.openInputStream(uri)?.use { exifInput ->
                    val exif = ExifInterface(exifInput)

                    when (
                        exif.getAttributeInt(
                            ExifInterface.TAG_ORIENTATION,
                            ExifInterface.ORIENTATION_NORMAL
                        )
                    ) {
                        ExifInterface.ORIENTATION_ROTATE_90,
                        ExifInterface.ORIENTATION_ROTATE_270 -> {
                            val tmp = width
                            width = height
                            height = tmp
                        }
                    }
                }

                width to height
            }
        } catch (_: Exception) {
            null
        }
    }
}


