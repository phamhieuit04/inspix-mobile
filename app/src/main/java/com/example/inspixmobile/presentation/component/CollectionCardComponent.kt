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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import com.example.inspixmobile.core.extension.noRippleClickable
import com.example.inspixmobile.core.extension.skeletonEffect
import com.example.inspixmobile.domain.model.Collection

@Composable
fun CollectionCardComponent(
    modifier: Modifier = Modifier,
    context: Context,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    collection: Collection,
    aspectRatio: Float,
    likeButtonVisible: Boolean = true,
    onClick: (Collection) -> Unit
) {
    var isLiked by remember(collection.uuid) { mutableStateOf(collection.isLiked ?: false) }
    var isImageLoaded by remember(collection.uuid) { mutableStateOf(false) }
    val hasLoadErrorState = remember(collection.uuid) { mutableStateOf(false) }
    val firstImage = collection.images?.firstOrNull()
    val thumbnailUrl = firstImage?.urlSmall ?: firstImage?.urlRegular ?: firstImage?.urlFull

    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(aspectRatio)
            .clip(RoundedCornerShape(12.dp))
    ) {
        if (!isImageLoaded && !hasLoadErrorState.value) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .skeletonEffect()
            )
        }

        if (hasLoadErrorState.value || thumbnailUrl == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFEAEAF0))
            )
        }

        if (!hasLoadErrorState.value && thumbnailUrl != null) {
            with(sharedTransitionScope) {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(thumbnailUrl)
                        .memoryCacheKey(firstImage?.uuid)
                        .placeholderMemoryCacheKey(firstImage?.uuid)
                        .build(),
                    contentDescription = collection.uuid,
                    contentScale = ContentScale.Crop,
                    onLoading = { isImageLoaded = false },
                    onSuccess = { isImageLoaded = true },
                    onError = { isImageLoaded = true; hasLoadErrorState.value = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .sharedElement(
                            sharedContentState = sharedTransitionScope.rememberSharedContentState(
                                key = firstImage?.uuid!!
                            ),
                            animatedVisibilityScope = animatedVisibilityScope,
                            boundsTransform = { _, _ ->
                                spring(
                                    dampingRatio = 0.85f,
                                    stiffness = Spring.StiffnessLow
                                )
                            },
                            clipInOverlayDuringTransition = OverlayClip(RoundedCornerShape(12.dp)),
                            renderInOverlayDuringTransition = true
                        )
                        .noRippleClickable { onClick(collection) }
                )
            }
        }

        if (likeButtonVisible) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(8.dp)
                    .size(36.dp)
                    .background(Color.White.copy(alpha = 0.85f), RoundedCornerShape(50))
                    .clip(RoundedCornerShape(50))
                    .clickable { isLiked = !isLiked },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = null,
                    tint = if (isLiked) Color(0xFFE53935) else Color(0xFF666666),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}