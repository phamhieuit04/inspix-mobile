package com.example.inspixmobile.presentation.screen

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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.toColorInt
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Bold
import com.adamglin.phosphoricons.bold.ArrowLeft
import com.adamglin.phosphoricons.bold.BookmarkSimple
import com.adamglin.phosphoricons.bold.Heart
import com.adamglin.phosphoricons.bold.Share
import com.composeunstyled.Text
import com.example.inspixmobile.core.extension.noRippleClickable
import com.example.inspixmobile.core.util.ImageHelper
import com.example.inspixmobile.domain.model.Collection
import com.example.inspixmobile.presentation.component.ShimmerGridItem
import com.example.inspixmobile.presentation.viewmodel.DetailCollectionViewModel
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.materials.CupertinoMaterials
import dev.chrisbanes.haze.materials.ExperimentalHazeMaterialsApi
import dev.chrisbanes.haze.rememberHazeState
import org.koin.compose.viewmodel.koinViewModel

@OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalHazeMaterialsApi::class
)
@Composable
fun DetailCollectionScreen(
    modifier: Modifier = Modifier,
    collection: Collection,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    navigateBack: () -> Unit,
    detailCollectionViewModel: DetailCollectionViewModel = koinViewModel()
) {
    val context = LocalContext.current

    val pagerState = rememberPagerState(
        initialPage = 0,
        pageCount = { collection.images?.count() ?: 0 }
    )
    val hazeState = rememberHazeState()
    val statusBarPadding = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()

    Box(modifier = modifier.fillMaxSize()) {
        LazyVerticalStaggeredGrid(
            modifier = Modifier.fillMaxSize(),
            columns = StaggeredGridCells.Fixed(2),
            contentPadding = PaddingValues(
                top = statusBarPadding,
                bottom = 16.dp,
                start = 8.dp,
                end = 8.dp
            ),
            verticalItemSpacing = 8.dp,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item(span = StaggeredGridItemSpan.FullLine) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(9f / 16f)
                ) {
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(12.dp))
                            .background(color = Color.Transparent)
                            .hazeSource(hazeState),
                    ) { page ->
                        with(sharedTransitionScope) {
                            val image = collection.images?.get(page)
                            val color = image?.color?.toColorInt()
                            val resolvedRatio = ImageHelper.aspectRatio(
                                image?.width,
                                image?.height
                            )

                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(color = Color(color!!))
                            ) {
                                AsyncImage(
                                    model = ImageRequest.Builder(context)
                                        .data(image.urlSmall)
                                        .memoryCacheKey(image.uuid!!)
                                        .placeholderMemoryCacheKey(image.uuid)
                                        .build(),
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .aspectRatio(resolvedRatio)
                                        .sharedElement(
                                            sharedContentState = rememberSharedContentState(
                                                key = image.uuid
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
                                )
                            }
                        }
                    }

                    AnimatedVisibility(
                        modifier = Modifier.align(Alignment.BottomCenter),
                        visible = animatedVisibilityScope.transition.currentState == animatedVisibilityScope.transition.targetState,
                        enter = fadeIn(animationSpec = tween(300)),
                        exit = fadeOut(animationSpec = tween(300))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 16.dp, end = 16.dp, bottom = 20.dp),
                            verticalAlignment = Alignment.Bottom,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            val author = collection.author
                            val avatarLoadError = remember { mutableStateOf(false) }

                            Row(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(50))
                                    .hazeEffect(
                                        state = hazeState,
                                        style = CupertinoMaterials.thin()
                                    )
                                    .padding(horizontal = 14.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .background(Color.White),
                                    contentAlignment = Alignment.Center
                                ) {
                                    val avatarUrl = author?.avatarUrl
                                    if (avatarUrl != null && !avatarLoadError.value) {
                                        AsyncImage(
                                            model = avatarUrl,
                                            contentDescription = null,
                                            contentScale = ContentScale.Crop,
                                            onError = { avatarLoadError.value = true },
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .clip(CircleShape)
                                        )
                                    }
                                }

                                Column(
                                    modifier = Modifier.padding(end = 8.dp),
                                    verticalArrangement = Arrangement.spacedBy(2.dp)
                                ) {
                                    Text(
                                        text = author?.name ?: "",
                                        color = Color.White,
                                        fontSize = 14.sp,
                                        maxLines = 1,
                                        fontWeight = FontWeight.SemiBold,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = author?.bio ?: "",
                                        color = Color.White.copy(alpha = 0.75f),
                                        fontSize = 12.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }

                            Column(
                                modifier = Modifier.padding(start = 12.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Box(
                                    modifier = Modifier
                                        .clip(CircleShape)
                                        .noRippleClickable(onClick = {})
                                        .hazeEffect(
                                            state = hazeState,
                                            style = CupertinoMaterials.thin()
                                        )
                                        .padding(14.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = PhosphorIcons.Bold.Heart,
                                        contentDescription = "Like",
                                        tint = Color.White,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .clip(CircleShape)
                                        .noRippleClickable(onClick = {})
                                        .hazeEffect(
                                            state = hazeState,
                                            style = CupertinoMaterials.thin()
                                        )
                                        .padding(14.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = PhosphorIcons.Bold.BookmarkSimple,
                                        contentDescription = "Bookmark",
                                        tint = Color.White,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .clip(CircleShape)
                                        .noRippleClickable(onClick = {})
                                        .hazeEffect(
                                            state = hazeState,
                                            style = CupertinoMaterials.thin()
                                        )
                                        .padding(14.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = PhosphorIcons.Bold.Share,
                                        contentDescription = "Share",
                                        tint = Color.White,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            items(10) { key ->
                ShimmerGridItem(index = key)
            }
        }

        AnimatedVisibility(
            modifier = Modifier.align(Alignment.TopStart),
            visible = animatedVisibilityScope.transition.currentState == animatedVisibilityScope.transition.targetState,
            enter = fadeIn(animationSpec = tween(300)),
            exit = fadeOut(animationSpec = tween(300))
        ) {
            Box(
                modifier = Modifier
                    .statusBarsPadding()
                    .padding(top = 12.dp, start = 20.dp)
                    .clip(CircleShape)
                    .noRippleClickable(navigateBack)
                    .hazeEffect(
                        state = hazeState,
                        style = CupertinoMaterials.thin()
                    )
                    .padding(14.dp)
            ) {
                Icon(
                    imageVector = PhosphorIcons.Bold.ArrowLeft,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }
        }
    }
}