package com.example.inspixmobile.presentation.component

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Bold
import com.adamglin.phosphoricons.Fill
import com.adamglin.phosphoricons.bold.ChatCircle
import com.adamglin.phosphoricons.bold.DownloadSimple
import com.adamglin.phosphoricons.bold.Heart
import com.adamglin.phosphoricons.fill.Heart
import com.example.inspixmobile.core.extension.noRippleClickable
import com.example.inspixmobile.core.extension.skeletonEffect
import com.example.inspixmobile.domain.model.Collection
import kotlin.collections.getOrNull
import kotlin.collections.orEmpty
import kotlin.collections.take

@Composable
fun CollectionFeedCardComponent(
    modifier: Modifier = Modifier,
    context: Context,
    collection: Collection,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    onClick: (Collection) -> Unit,
    onShowComments: (Collection) -> Unit
) {
    var isLiked by remember(collection.uuid, collection.isLiked) {
        mutableStateOf(
            collection.isLiked ?: false
        )
    }
    val images = collection.images.orEmpty()
    val displayImages = images.take(3)
    val totalImages = images.size
    val hasMore = totalImages > 3
    val pageCount = maxOf(1, if (hasMore) displayImages.size + 1 else displayImages.size)
    val pagerState = rememberPagerState(pageCount = { pageCount })
    val showAllBgImage = images.getOrNull(3) ?: images.getOrNull(2)
    val isOnShowAllPage = hasMore && pagerState.currentPage == displayImages.size
    val fixedFeedRatio = 3f / 4f

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF7B4FBF).copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                val avatarUrl = collection.author?.avatarUrl
                if (avatarUrl != null) {
                    AsyncImage(
                        model = avatarUrl,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                    )
                } else {
                    Text(
                        text = collection.author?.name?.take(1)?.uppercase() ?: "U",
                        color = Color(0xFF7B4FBF),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(1.dp)
            ) {
                Text(
                    text = collection.author?.name ?: "Unknown",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF1A1A2E),
                    lineHeight = 16.sp
                )
                if (!collection.title.isNullOrBlank()) {
                    Text(
                        text = collection.title,
                        fontSize = 11.sp,
                        color = Color(0xFF888899),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        lineHeight = 14.sp
                    )
                }
            }

            Text(
                text = collection.createdAtHuman ?: collection.createdAt ?: "",
                fontSize = 11.sp,
                color = Color(0xFF888899),
                textAlign = TextAlign.End
            )
        }

        val imageLoadedStates = remember(collection.uuid) {
            Array(displayImages.size) { false }
        }

        Box(modifier = Modifier.fillMaxWidth()) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxWidth(),
                key = { page -> page }
            ) { page ->
                if (hasMore && page == displayImages.size) {
                    val bgUrl = showAllBgImage?.urlSmall
                        ?: showAllBgImage?.urlRegular
                        ?: showAllBgImage?.urlFull

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(fixedFeedRatio),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color(0xFF2A1A4A))
                        )
                        if (bgUrl != null) {
                            AsyncImage(
                                model = bgUrl,
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .graphicsLayer { alpha = 0.35f }
                            )
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.2f))
                        )

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(Color.White.copy(alpha = 0.25f))
                                .noRippleClickable { onClick(collection) }
                                .padding(horizontal = 24.dp, vertical = 12.dp)
                        ) {
                            Text(
                                text = "Xem tất cả $totalImages ảnh",
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                } else {
                    val image = displayImages.getOrNull(page)
                    if (image == null) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(fixedFeedRatio)
                                .background(Color(0xFFEAEAF0))
                        )
                        return@HorizontalPager
                    }
                    val imageKey = image.uuid ?: "${collection.uuid}-page-$page"
                    val imageUrl = image.urlSmall ?: image.urlRegular ?: image.urlFull
                    var isLoaded by remember(collection.uuid, page) {
                        mutableStateOf(imageLoadedStates.getOrElse(page) { false })
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(fixedFeedRatio)
                    ) {
                        if (!isLoaded) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .skeletonEffect()
                            )
                        }

                        if (imageUrl != null) {
                            with(sharedTransitionScope) {
                                AsyncImage(
                                    model = ImageRequest.Builder(context)
                                        .data(image.urlSmall)
                                        .memoryCacheKey(imageKey)
                                        .placeholderMemoryCacheKey(imageKey)
                                        .build(),
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    onSuccess = {
                                        isLoaded = true
                                        if (page < imageLoadedStates.size) imageLoadedStates[page] =
                                            true
                                    },
                                    onError = {
                                        isLoaded = true
                                        if (page < imageLoadedStates.size) imageLoadedStates[page] =
                                            true
                                    },
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .sharedElement(
                                            sharedContentState = rememberSharedContentState(
                                                key = imageKey
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
                                            renderInOverlayDuringTransition = true,
                                            zIndexInOverlay = 0f
                                        )
                                        .noRippleClickable(onClick = { onClick(collection) })
                                )
                            }
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color(0xFFEAEAF0))
                            )
                        }

                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(10.dp)
                                .clip(RoundedCornerShape(50))
                                .background(Color.Black.copy(alpha = 0.4f))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "${page + 1}/$totalImages",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            this@Column.AnimatedVisibility(
                visible = !isOnShowAllPage,
                enter = fadeIn(animationSpec = tween(200)),
                exit = fadeOut(animationSpec = tween(200)),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .background(Color.White.copy(alpha = 0.85f), CircleShape)
                        .noRippleClickable { isLiked = !isLiked },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = null,
                        tint = if (isLiked) Color(0xFFE53935) else Color(0xFF888899),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp),
                modifier = Modifier.noRippleClickable { isLiked = !isLiked }
            ) {
                Icon(
                    imageVector = if (isLiked) PhosphorIcons.Fill.Heart else PhosphorIcons.Bold.Heart,
                    contentDescription = null,
                    tint = if (isLiked) Color(0xFFE53935) else Color(0xFF888899),
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = "${collection.totalLikes ?: 0}",
                    fontSize = 13.sp,
                    color = Color(0xFF444455),
                    fontWeight = FontWeight.Medium
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp),
                modifier = Modifier.noRippleClickable(onClick = { onShowComments(collection) })
            ) {
                Icon(
                    imageVector = PhosphorIcons.Bold.ChatCircle,
                    contentDescription = null,
                    tint = Color(0xFF888899),
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = "${collection.totalComments ?: 0}",
                    fontSize = 13.sp,
                    color = Color(0xFF444455),
                    fontWeight = FontWeight.Medium
                )
            }

            Icon(
                imageVector = PhosphorIcons.Bold.DownloadSimple,
                contentDescription = null,
                tint = Color(0xFF888899),
                modifier = Modifier
                    .size(18.dp)
                    .noRippleClickable { }
            )
        }

        if (!collection.description.isNullOrBlank()) {
            Text(
                text = collection.description,
                fontSize = 12.sp,
                color = Color(0xFF444455),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 12.dp)
            )
        }
    }
}
