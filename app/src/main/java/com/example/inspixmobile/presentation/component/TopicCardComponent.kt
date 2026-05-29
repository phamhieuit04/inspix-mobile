package com.example.inspixmobile.presentation.component

import android.content.Context
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import com.example.inspixmobile.domain.model.Topic

@Composable
fun TopicCardComponent(
    modifier: Modifier = Modifier,
    context: Context,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    topic: Topic,
    aspectRatio: Float = 3f / 2f,
    fontSize: TextUnit = 14.sp,
    onClick: () -> Unit
) {
    val thumbnailUrl = topic.thumbnailUrl
    val topicId = topic.id
    val title = topic.name

    with(sharedTransitionScope) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(aspectRatio)
                .clip(shape = RoundedCornerShape(12.dp))
                .clickable(onClick = onClick)
                .sharedElement(
                    sharedContentState = rememberSharedContentState(
                        key = "topic_${topicId}"
                    ),
                    animatedVisibilityScope = animatedVisibilityScope,
                    boundsTransform = { _, _ ->
                        spring(
                            dampingRatio = 0.85f,
                            stiffness = Spring.StiffnessLow
                        )
                    },
                    clipInOverlayDuringTransition = OverlayClip(
                        RoundedCornerShape(12.dp)
                    ),
                    renderInOverlayDuringTransition = true
                )
        ) {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(thumbnailUrl)
                    .memoryCacheKey("topic_${topicId}")
                    .placeholderMemoryCacheKey("topic_${topicId}")
                    .build(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .drawWithContent {
                        drawContent()

                        drawRect(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color.Transparent,
                                    Color.Black.copy(alpha = 0.4f)
                                )
                            )
                        )
                    }
            )

            Text(
                text = title ?: "Topic vô danh",
                color = Color.White,
                fontSize = fontSize,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(vertical = 12.dp, horizontal = 16.dp)
            )
        }
    }
}