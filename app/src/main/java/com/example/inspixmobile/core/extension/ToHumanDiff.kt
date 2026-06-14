package com.example.inspixmobile.core.extension

import java.time.Duration
import java.time.OffsetDateTime

fun String.toHumanDiff(): String {
    return try {
        val time = OffsetDateTime.parse(this)
        val now = OffsetDateTime.now()

        val duration = Duration.between(time, now)

        when {
            duration.seconds < 60 ->
                "Vừa xong"

            duration.toMinutes() < 60 ->
                "${duration.toMinutes()} phút trước"

            duration.toHours() < 24 ->
                "${duration.toHours()} giờ trước"

            duration.toDays() < 30 ->
                "${duration.toDays()} ngày trước"

            duration.toDays() < 365 ->
                "${duration.toDays() / 30} tháng trước"

            else ->
                "${duration.toDays() / 365} năm trước"
        }
    } catch (_: Exception) {
        ""
    }
}