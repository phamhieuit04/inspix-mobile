package com.example.inspixmobile.presentation.component

import android.util.Log
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import com.example.inspixmobile.core.extension.noRippleClickable
import com.example.inspixmobile.core.extension.skeletonEffect
import com.example.inspixmobile.domain.model.Collection
import com.example.inspixmobile.domain.model.User

@Composable
fun RecommendedArtistCardComponent(
    modifier: Modifier = Modifier,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    collections: List<Collection>,
    isFollowed: Boolean = false,
    onFollow: (User) -> Unit,
    navigateToDetailArtist: (User) -> Unit,
    navigateToDetailCollection: (Collection) -> Unit
) {
    val context = LocalContext.current

    val artist = collections.firstOrNull()?.author ?: return
    val displayCollections = collections.take(3)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(color = Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp)
                .noRippleClickable(onClick = { navigateToDetailArtist(artist) }),
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
                val avatarUrl = artist.avatarUrl
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
                        text = artist.name?.take(1)?.uppercase() ?: "U",
                        color = Color(0xFF7B4FBF),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Text(
                text = artist.name ?: "Unknown",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF1A1A2E),
                lineHeight = 16.sp
            )

            Spacer(modifier = Modifier.weight(1f))

            FollowButtonComponent(
                isFollowed = isFollowed,
                size = FollowButtonSize.Small,
                onClick = { onFollow(artist) }
            )
        }

        Row(modifier = Modifier.fillMaxWidth()) {
            displayCollections.forEach { collection ->
                val firstImage = collection.images?.firstOrNull()
                val imageKey = firstImage?.uuid ?: collection.uuid ?: ""
                val imageUrl = firstImage?.urlSmall ?: firstImage?.urlRegular ?: firstImage?.urlFull
                var isLoaded by remember(imageKey) { mutableStateOf(false) }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .aspectRatio(1f)
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
                                    .data(imageUrl)
                                    .memoryCacheKey(imageKey)
                                    .placeholderMemoryCacheKey(imageKey)
                                    .build(),
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                onSuccess = { isLoaded = true },
                                onError = { isLoaded = true },
                                modifier = Modifier
                                    .fillMaxSize()
                                    .sharedElement(
                                        sharedContentState = rememberSharedContentState(key = imageKey),
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
                                    .noRippleClickable { navigateToDetailCollection(collection) }
                            )
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color(0xFFEAEAF0))
                        )
                    }
                }
            }
        }
    }
}