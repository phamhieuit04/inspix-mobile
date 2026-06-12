package com.example.inspixmobile.presentation.screen

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.EnterExitState
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
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
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.toColorInt
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Bold
import com.adamglin.phosphoricons.Fill
import com.adamglin.phosphoricons.bold.ArrowDown
import com.adamglin.phosphoricons.bold.ChatCircle
import com.adamglin.phosphoricons.bold.Heart
import com.adamglin.phosphoricons.fill.Heart
import com.composeunstyled.Text
import com.example.inspixmobile.core.extension.formatCompact
import com.example.inspixmobile.core.extension.noRippleClickable
import com.example.inspixmobile.core.util.ImageHelper
import com.example.inspixmobile.domain.model.Collection
import com.example.inspixmobile.presentation.component.BackScaffold
import com.example.inspixmobile.presentation.component.CollectionCardComponent
import com.example.inspixmobile.presentation.component.ShimmerGridItem
import com.example.inspixmobile.presentation.viewmodel.CommentSheetViewModel
import com.example.inspixmobile.presentation.viewmodel.DetailCollectionViewModel
import dev.chrisbanes.haze.materials.ExperimentalHazeMaterialsApi
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
    navigateToDetailCollection: (Collection) -> Unit,
    navigateBack: () -> Unit,
    detailCollectionViewModel: DetailCollectionViewModel = koinViewModel(),
    commentSheetViewModel: CommentSheetViewModel = koinViewModel()
) {
    val context = LocalContext.current
    val pagerState = rememberPagerState(
        initialPage = 0,
        pageCount = { collection.images?.count() ?: 0 }
    )

    val statusBarPadding = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val backgroundColor = Color(0xFFe8e8e9)
    val iconColor = Color.DarkGray

    val showOverlayRaw by remember {
        derivedStateOf {
            animatedVisibilityScope.transition.targetState == EnterExitState.Visible
        }
    }
    var showOverlayDelayed by remember { mutableStateOf(true) }

    val exploreCollectionsFlow = remember(collection.uuid) {
        detailCollectionViewModel.getExploreCollectionsPaging(collection.uuid!!)
    }
    val exploreCollections = exploreCollectionsFlow.collectAsLazyPagingItems()
    val exploreLoading =
        exploreCollections.loadState.refresh is LoadState.Loading && exploreCollections.itemCount == 0

    val interactions by detailCollectionViewModel.interactions.collectAsState()

    val interaction = interactions[collection.uuid]
    val isLiked =
        interaction?.isLiked ?: (collection.isLiked
            ?: false)
    val totalLikes =
        interaction?.totalLikes ?: collection.totalLikes
    val totalComments =
        interaction?.totalComments ?: collection.totalComments

    val latestComment = collection.lastestComment

    LaunchedEffect(showOverlayRaw) {
        if (showOverlayRaw) {
            showOverlayDelayed = true
        } else {
            showOverlayDelayed = false
        }
    }

    BackHandler { navigateBack() }

    BackScaffold(
        sharedTransitionScope = sharedTransitionScope,
        isShowOverlayDelayed = showOverlayDelayed,
        onBackPressed = navigateBack
    ) {
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
            item(
                key = "collection-header-${collection.uuid}",
                span = StaggeredGridItemSpan.FullLine
            ) {
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
                            enter = EnterTransition.None,
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
                                        .shadow(
                                            elevation = 6.dp,
                                            shape = CircleShape,
                                            clip = false
                                        )
                                        .background(
                                            color = backgroundColor,
                                            shape = CircleShape
                                        )
                                        .clip(CircleShape)
                                        .clickable(onClick = { })
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
                                                model = ImageRequest.Builder(context)
                                                    .data(avatarUrl)
                                                    .size(88)
                                                    .crossfade(true)
                                                    .build(),
                                                contentDescription = null,
                                                contentScale = ContentScale.Crop,
                                                onError = { avatarLoadError.value = true },
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .clip(CircleShape)
                                            )
                                        } else {
                                            Text(
                                                text = author?.name?.take(1)?.uppercase()
                                                    ?: "U",
                                                color = Color(0xFF7B4FBF),
                                                fontSize = 18.sp,
                                                fontWeight = FontWeight.SemiBold
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
                                            color = iconColor,
                                            fontSize = 14.sp,
                                            maxLines = 1,
                                            fontWeight = FontWeight.SemiBold,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        if (!author?.bio.isNullOrEmpty()) {
                                            Text(
                                                text = author.bio,
                                                color = iconColor.copy(alpha = 0.75f),
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
                                            .shadow(
                                                elevation = 6.dp,
                                                shape = CircleShape,
                                                clip = false
                                            )
                                            .background(
                                                color = backgroundColor,
                                                shape = CircleShape
                                            )
                                            .clip(CircleShape)
                                            .clickable(onClick = {
                                                detailCollectionViewModel.toggleLike(collection)
                                            })
                                            .padding(14.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = if (isLiked) PhosphorIcons.Fill.Heart else PhosphorIcons.Bold.Heart,
                                            contentDescription = "Like",
                                            tint = if (isLiked) Color.Red else iconColor,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }

                                    if (totalLikes != null && totalLikes > 0) {
                                        Text(
                                            text = totalLikes.formatCompact(),
                                            fontSize = 13.sp,
                                            color = Color.White,
                                            fontWeight = FontWeight.Medium,
                                            style = TextStyle(
                                                shadow = Shadow(
                                                    color = Color.Black.copy(alpha = 0.6f),
                                                    offset = Offset(2f, 2f),
                                                    blurRadius = 4f
                                                )
                                            )
                                        )
                                    }

                                    Box(
                                        modifier = Modifier
                                            .shadow(
                                                elevation = 6.dp,
                                                shape = CircleShape,
                                                clip = false
                                            )
                                            .background(
                                                color = backgroundColor,
                                                shape = CircleShape
                                            )
                                            .clip(CircleShape)
                                            .clickable(onClick = {
                                                commentSheetViewModel.show(collection.uuid!!)
                                            })
                                            .padding(14.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = PhosphorIcons.Bold.ChatCircle,
                                            contentDescription = "Comment",
                                            tint = iconColor,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }

                                    if (totalComments != null && totalComments > 0) {
                                        Text(
                                            text = totalComments.formatCompact(),
                                            fontSize = 13.sp,
                                            color = Color.White,
                                            fontWeight = FontWeight.Medium,
                                            style = TextStyle(
                                                shadow = Shadow(
                                                    color = Color.Black.copy(alpha = 0.6f),
                                                    offset = Offset(2f, 2f),
                                                    blurRadius = 4f
                                                )
                                            )
                                        )
                                    }

                                    Box(
                                        modifier = Modifier
                                            .shadow(
                                                elevation = 6.dp,
                                                shape = CircleShape,
                                                clip = false
                                            )
                                            .background(
                                                color = backgroundColor,
                                                shape = CircleShape
                                            )
                                            .clip(CircleShape)
                                            .clickable(onClick = { })
                                            .padding(14.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = PhosphorIcons.Bold.ArrowDown,
                                            contentDescription = "Download",
                                            tint = iconColor,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            item(
                key = "collection-info-${collection.uuid}",
                span = StaggeredGridItemSpan.FullLine
            ) {
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
                        lineHeight = 30.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    if (!collection.description.isNullOrEmpty()) {
                        Text(
                            text = collection.description,
                            fontSize = 14.sp,
                            color = Color(0xFF666666),
                            lineHeight = 22.sp
                        )
                        Spacer(modifier = Modifier.height(28.dp))
                    }

                    if (latestComment != null) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color(0xFF7B4FBF).copy(alpha = 0.1f))
                                .padding(horizontal = 16.dp, vertical = 16.dp)
                                .noRippleClickable(onClick = {
                                    commentSheetViewModel.show(
                                        collection.uuid!!
                                    )
                                })
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Text(
                                        text = "Bình luận",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF111111)
                                    )
                                    Box(
                                        modifier = Modifier
                                            .clip(CircleShape)
                                            .background(Color(0xFF222222))
                                            .padding(horizontal = 8.dp, vertical = 3.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = collection.totalComments.toString(),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Color.White
                                        )
                                    }
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    val commentUser = latestComment.user
                                    val commentAvatarUrl = commentUser?.avatarUrl
                                    val avatarLoadError = remember(latestComment.id) {
                                        mutableStateOf(false)
                                    }
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFFCCCCCC)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (commentAvatarUrl != null && !avatarLoadError.value) {
                                            AsyncImage(
                                                model = ImageRequest.Builder(context)
                                                    .data(commentAvatarUrl)
                                                    .size(88)
                                                    .crossfade(true)
                                                    .build(),
                                                contentDescription = null,
                                                contentScale = ContentScale.Crop,
                                                onError = { avatarLoadError.value = true },
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .clip(CircleShape)
                                            )
                                        } else {
                                            Text(
                                                text = commentUser?.name?.take(1)?.uppercase()
                                                    ?: "U",
                                                color = Color(0xFF7B4FBF),
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        }
                                    }
                                    Text(
                                        text = latestComment.content ?: "",
                                        fontSize = 14.sp,
                                        color = Color(0xFF333333)
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(28.dp))
                    }

                    Spacer(modifier = Modifier.height(20.dp))
                }
            }

            val isLoading = exploreLoading
            val isError = exploreCollections.loadState.refresh is LoadState.Error

            if (!isError) {
                item(
                    key = "collection-explore-title-${collection.uuid}",
                    span = StaggeredGridItemSpan.FullLine
                ) {
                    Text(
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                            .padding(bottom = 8.dp),
                        text = "Có thể bạn cũng thích",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF111111)
                    )
                }

                if (isLoading) {
                    items(
                        count = 30,
                        key = { index -> "collection-explore-shimmer-$index" }
                    ) { index ->
                        ShimmerGridItem(index = index)
                    }
                } else {
                    items(count = exploreCollections.itemCount) { index ->
                        val exploreCollection = exploreCollections[index] ?: return@items
                        val coverImage = exploreCollection.images?.firstOrNull()
                        val resolvedRatio = ImageHelper.aspectRatio(
                            coverImage?.width,
                            coverImage?.height
                        )

                        val interaction = interactions[exploreCollection.uuid]
                        val isLiked =
                            interaction?.isLiked ?: (exploreCollection.isLiked
                                ?: false)

                        CollectionCardComponent(
                            context = context,
                            sharedTransitionScope = sharedTransitionScope,
                            animatedVisibilityScope = animatedVisibilityScope,
                            collection = exploreCollection,
                            aspectRatio = resolvedRatio,
                            isLiked = isLiked,
                            onClick = {
                                navigateToDetailCollection(exploreCollection)
                            },
                            onToggleLike = {
                                detailCollectionViewModel.toggleLike(exploreCollection)
                            }
                        )
                    }
                }
            }
        }
    }
}
