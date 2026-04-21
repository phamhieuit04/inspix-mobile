package com.example.inspixmobile.core.util

import com.example.inspixmobile.data.source.remote.config.API_ENDPOINT

object UrlHelper {
    fun resolveMediaUrl(path: String?): String? {
        val value = path?.trim().orEmpty()
        if (value.isEmpty()) return null

        val isAbsolute = value.startsWith("http://", ignoreCase = true) ||
                value.startsWith("https://", ignoreCase = true)
        if (isAbsolute) return value

        val normalizedPath = if (value.startsWith('/')) value else "/$value"
        return "${API_ENDPOINT}$normalizedPath"
    }
}

