package com.example.inspixmobile.core.extension

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.net.Uri
import androidx.core.net.toUri
import java.io.File

fun Bitmap.rotateBitmap(rotationDegrees: Int): Bitmap {
    val matrix = Matrix().apply {
        postRotate(-rotationDegrees.toFloat())
        postScale(-1f, -1f)
    }

    return Bitmap.createBitmap(this, 0, 0, width, height, matrix, true)
}

fun Bitmap.saveToCache(
    context: Context
): Uri {
    val file = File(
        context.cacheDir,
        "capture_${System.currentTimeMillis()}.jpg"
    )

    file.outputStream().use { output ->
        compress(
            Bitmap.CompressFormat.JPEG,
            95,
            output
        )
    }

    return file.toUri()
}