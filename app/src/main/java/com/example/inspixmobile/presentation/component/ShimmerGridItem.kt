package com.example.inspixmobile.presentation.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.example.inspixmobile.core.extension.skeletonEffect

@Composable
fun ShimmerGridItem(index: Int) {
    val ratio = if (index % 3 == 0) 0.75f else if (index % 3 == 1) 1.2f else 1.0f
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(ratio)
            .clip(RoundedCornerShape(12.dp))
            .skeletonEffect()
    )
}