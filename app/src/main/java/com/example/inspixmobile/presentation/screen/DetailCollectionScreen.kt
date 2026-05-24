package com.example.inspixmobile.presentation.screen

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.EnterExitState
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.toColorInt
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Bold
import com.adamglin.phosphoricons.Fill
import com.adamglin.phosphoricons.bold.ArrowDown
import com.adamglin.phosphoricons.bold.ArrowLeft
import com.adamglin.phosphoricons.bold.BookmarkSimple
import com.adamglin.phosphoricons.bold.ChatCircle
import com.adamglin.phosphoricons.bold.Download
import com.adamglin.phosphoricons.bold.DownloadSimple
import com.adamglin.phosphoricons.bold.Heart
import com.adamglin.phosphoricons.bold.Share
import com.adamglin.phosphoricons.fill.Heart
import com.composeunstyled.Text
import com.example.inspixmobile.core.extension.noRippleClickable
import com.example.inspixmobile.core.util.ImageHelper
import com.example.inspixmobile.domain.model.Collection
import com.example.inspixmobile.presentation.component.ShimmerGridItem
import com.example.inspixmobile.presentation.component.TopShadowOverlay
import com.example.inspixmobile.presentation.viewmodel.DetailCollectionViewModel
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.materials.CupertinoMaterials
import dev.chrisbanes.haze.materials.ExperimentalHazeMaterialsApi
import dev.chrisbanes.haze.rememberHazeState
import kotlinx.coroutines.delay
import org.koin.compose.viewmodel.koinViewModel

@OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalHazeMaterialsApi::class,
    ExperimentalLayoutApi::class
)
@Composable
fun DetailCollectionScreen(
    modifier: Modifier = Modifier,
    collection: Collection,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    bottomContentPadding: Dp = 8.dp,
    navigateBack: () -> Unit,
    detailCollectionViewModel: DetailCollectionViewModel = koinViewModel()
) {
    val context = LocalContext.current

    val pagerState = rememberPagerState(
        initialPage = 0,
        pageCount = { collection.images?.count() ?: 0 }
    )
    val hazeState = rememberHazeState()
    val hazeStyle = CupertinoMaterials.thin()

    val statusBarPadding = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()

    val showOverlayRaw by remember {
        derivedStateOf {
            animatedVisibilityScope.transition.targetState == EnterExitState.Visible
        }
    }
    var showOverlayDelayed by remember { mutableStateOf(false) }

    var isLiked by remember(collection.uuid) { mutableStateOf(collection.isLiked ?: false) }

    LaunchedEffect(showOverlayRaw) {
        if (showOverlayRaw) {
            showOverlayDelayed = false
            delay(400)
            showOverlayDelayed = true
        } else {
            showOverlayDelayed = false
        }
    }

    BackHandler { navigateBack() }

    Box(modifier = modifier.fillMaxSize()) {
        LazyVerticalStaggeredGrid(
            modifier = Modifier.fillMaxSize(),
            columns = StaggeredGridCells.Fixed(2),
            contentPadding = PaddingValues(
                top = statusBarPadding,
                bottom = bottomContentPadding + 16.dp,
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
                            .background(color = Color.Transparent),
                    ) { page ->
                        with(sharedTransitionScope) {
                            val image = requireNotNull(collection.images?.get(page))
                            val imageKey = requireNotNull(image.uuid)
                            val color = image.color?.toColorInt()
                            val resolvedRatio = ImageHelper.aspectRatio(
                                image.width,
                                image.height
                            )

                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(color = Color(color ?: 0xFF000000.toInt()))
                                    .hazeSource(hazeState)
                            ) {
                                AsyncImage(
                                    model = ImageRequest.Builder(context)
                                        .data(image.urlSmall)
                                        .memoryCacheKey(imageKey)
                                        .placeholderMemoryCacheKey(imageKey)
                                        .build(),
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .aspectRatio(resolvedRatio)
                                        .hazeSource(hazeState)
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
                                )
                            }
                        }
                    }

                    with(sharedTransitionScope) {
                        AnimatedVisibility(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .renderInSharedTransitionScopeOverlay(zIndexInOverlay = 1f),
                            visible = showOverlayDelayed,
                            enter = fadeIn(animationSpec = tween(220)),
                            exit = ExitTransition.None
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
                                        .hazeEffect(state = hazeState, style = hazeStyle)
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
                                        verticalArrangement = Arrangement.spacedBy(
                                            space = 2.dp,
                                            alignment = Alignment.CenterVertically
                                        )
                                    ) {
                                        Text(
                                            text = author?.name ?: "Nghệ sĩ vô danh",
                                            color = Color.White,
                                            fontSize = 14.sp,
                                            maxLines = 1,
                                            fontWeight = FontWeight.SemiBold,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        if (!author?.bio.isNullOrEmpty()) {
                                            Text(
                                                text = author.bio,
                                                color = Color.White.copy(alpha = 0.75f),
                                                fontSize = 12.sp,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
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
                                            .hazeEffect(state = hazeState, style = hazeStyle)
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
                                            .hazeEffect(state = hazeState, style = hazeStyle)
                                            .padding(14.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = PhosphorIcons.Bold.ChatCircle,
                                            contentDescription = "Comment",
                                            tint = Color.White,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }

                                    Box(
                                        modifier = Modifier
                                            .clip(CircleShape)
                                            .noRippleClickable(onClick = {})
                                            .hazeEffect(state = hazeState, style = hazeStyle)
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
                                            .hazeEffect(state = hazeState, style = hazeStyle)
                                            .padding(14.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = PhosphorIcons.Bold.ArrowDown,
                                            contentDescription = "Download",
                                            tint = Color.White,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            item(span = StaggeredGridItemSpan.FullLine) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp, start = 16.dp, end = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(0.dp)
                ) {
                    Text(
                        text = collection.title?.uppercase() ?: "BỘ SƯU TẬP",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF111111),
                        lineHeight = 26.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    if (!collection.description.isNullOrEmpty()) {
                        Text(
                            text = collection.description,
                            fontSize = 14.sp,
                            color = Color(0xFF666666),
                            lineHeight = 22.sp
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Có thể bạn cũng thích",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF111111)
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            items(10) { index ->
                ShimmerGridItem(index = index)
            }
        }

        TopShadowOverlay()

        with(sharedTransitionScope) {
            AnimatedVisibility(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .renderInSharedTransitionScopeOverlay(zIndexInOverlay = 1f),
                visible = showOverlayDelayed,
                enter = fadeIn(animationSpec = tween(220)),
                exit = ExitTransition.None
            ) {
                Box(
                    modifier = Modifier
                        .statusBarsPadding()
                        .padding(top = 12.dp, start = 20.dp)
                        .clip(CircleShape)
                        .noRippleClickable(navigateBack)
                        .hazeEffect(state = hazeState, style = CupertinoMaterials.thin())
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
}