package com.example.inspixmobile.core.extension

fun Int.formatCompact(): String {
    return when {
        this >= 1_000_000_000 -> "%.1fB".format(this / 1_000_000_000.0)
        this >= 1_000_000 -> "%.1fM".format(this / 1_000_000.0)
        this >= 1_000 -> "%.1fK".format(this / 1_000.0)
        else -> this.toString()
    }.removeSuffix(".0K")
        .removeSuffix(".0M")
        .removeSuffix(".0B")
}