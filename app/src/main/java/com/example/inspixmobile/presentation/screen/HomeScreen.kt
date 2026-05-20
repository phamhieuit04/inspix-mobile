package com.example.inspixmobile.presentation.screen

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.core.updateTransition
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.ViewAgenda
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import coil3.compose.AsyncImage
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
import com.example.inspixmobile.presentation.viewmodel.HomeViewModel
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.materials.CupertinoMaterials
import dev.chrisbanes.haze.materials.ExperimentalHazeMaterialsApi
import dev.chrisbanes.haze.rememberHazeState
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.geometry.Offset
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.inspixmobile.core.util.ImageHelper
import com.example.inspixmobile.domain.model.Topic
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel

enum class HomeLayoutStyle { Grid, Feed }

private const val HOME_PAGE_SIZE = 20
private const val HOME_PREFETCH_DISTANCE = 10
private const val HOME_TOPICS_ALL = "Tất cả"

@Composable
fun HomeScreen(
    bottomContentPadding: Dp = 8.dp,
    homeViewModel: HomeViewModel = koinViewModel()
) {
    var selectedTopic by remember { mutableIntStateOf(0) }
    val pagingCollections = remember(homeViewModel, selectedTopic) {
        if (selectedTopic == 0) {
            homeViewModel.getCollectionsPaging(
                pageSize = HOME_PAGE_SIZE,
                prefetchDistance = HOME_PREFETCH_DISTANCE
            )
        } else {
            homeViewModel.getCollectionsPagingByTopic(
                topicId = selectedTopic,
                pageSize = HOME_PAGE_SIZE,
                prefetchDistance = HOME_PREFETCH_DISTANCE
            )
        }
    }.collectAsLazyPagingItems()
    val topics by remember(homeViewModel) {
        homeViewModel.getTopics()
    }.collectAsStateWithLifecycle()
    val displayTopics = remember(topics) { ensureAllTopic(topics) }

    val density = LocalDensity.current
    val scope = rememberCoroutineScope()
    val hazeState = rememberHazeState()
    var isSearchBarVisible by remember { mutableStateOf(true) }

    var layoutStyle by remember { mutableStateOf(HomeLayoutStyle.Grid) }
    var headerHeightPx by remember { mutableIntStateOf(0) }
    val headerHeightDp = with(density) { headerHeightPx.toDp() }
    val gridState = rememberLazyStaggeredGridState()
    val pullToRefreshState = rememberPullToRefreshState()
    val isRefreshing = pagingCollections.loadState.refresh is LoadState.Loading
    var userRefreshRequested by remember { mutableStateOf(false) }
    val indicatorRefreshing = userRefreshRequested && isRefreshing
    val showRefreshIndicator = indicatorRefreshing || pullToRefreshState.distanceFraction > 0f

    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            private var accumulatedDown = 0f
            private var accumulatedUp = 0f
            val threshold = with(density) { 40.dp.toPx() }

            override fun onPreScroll(
                available: Offset,
                source: NestedScrollSource
            ): Offset {
                val delta = available.y
                if (delta < 0) {
                    accumulatedDown += -delta
                    accumulatedUp = 0f
                    if (accumulatedDown >= threshold) {
                        isSearchBarVisible = false
                        accumulatedDown = 0f
                    }
                } else if (delta > 0) {
                    accumulatedUp += delta
                    accumulatedDown = 0f
                    if (accumulatedUp >= threshold) {
                        isSearchBarVisible = true
                        accumulatedUp = 0f
                    }
                }
                return Offset.Zero
            }
        }
    }

    LaunchedEffect(isRefreshing) {
        if (!isRefreshing) {
            userRefreshRequested = false
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF0F0F5))
            .nestedScroll(nestedScrollConnection)
    ) {
        PullToRefreshBox(
            isRefreshing = indicatorRefreshing,
            onRefresh = {
                userRefreshRequested = true
                scope.launch {
                    gridState.scrollToItem(0)
                    homeViewModel.refreshTopics()
                    pagingCollections.refresh()
                }
            },
            state = pullToRefreshState,
            indicator = {
                if (showRefreshIndicator) {
                    PullToRefreshDefaults.Indicator(
                        state = pullToRefreshState,
                        isRefreshing = indicatorRefreshing,
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = headerHeightDp + 8.dp)
                    )
                }
            },
            modifier = Modifier.fillMaxSize()
        ) {
            AnimatedContent(
                targetState = layoutStyle,
                transitionSpec = {
                    fadeIn(animationSpec = tween(durationMillis = 400)) togetherWith
                            fadeOut(animationSpec = tween(durationMillis = 300))
                },
                label = "layout_transition"
            ) { currentLayout ->
                LazyVerticalStaggeredGrid(
                    columns = when (currentLayout) {
                        HomeLayoutStyle.Grid -> StaggeredGridCells.Fixed(2)
                        HomeLayoutStyle.Feed -> StaggeredGridCells.Fixed(1)
                    },
                    state = gridState,
                    contentPadding = PaddingValues(
                        top = headerHeightDp + 8.dp,
                        start = if (currentLayout == HomeLayoutStyle.Grid) 8.dp else 0.dp,
                        end = if (currentLayout == HomeLayoutStyle.Grid) 8.dp else 0.dp,
                        bottom = bottomContentPadding + 16.dp
                    ),
                    horizontalArrangement = if (currentLayout == HomeLayoutStyle.Grid)
                        Arrangement.spacedBy(8.dp) else Arrangement.Start,
                    verticalItemSpacing = if (currentLayout == HomeLayoutStyle.Grid) 8.dp else 16.dp,
                    modifier = Modifier
                        .fillMaxSize()
                        .hazeSource(state = hazeState)
                ) {
                    items(count = pagingCollections.itemCount) { index ->
                        val collection = pagingCollections[index]
                        when (currentLayout) {
                            HomeLayoutStyle.Grid -> {
                                if (collection != null) {
                                    val coverImage = collection.images?.firstOrNull()
                                    val resolvedRatio = ImageHelper.aspectRatio(
                                        coverImage?.width,
                                        coverImage?.height
                                    )
                                    CollectionCard(
                                        collection = collection,
                                        aspectRatio = resolvedRatio
                                    )
                                }
                            }

                            HomeLayoutStyle.Feed -> {
                                if (collection != null) {
                                    CollectionFeedCard(collection = collection)
                                }
                            }
                        }
                    }
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0xFFF0F0F5), Color.Transparent)
                    )
                )
        )

        HomeHeader(
            modifier = Modifier.onSizeChanged { headerHeightPx = it.height },
            topics = displayTopics.take(6),
            selectedTopic = selectedTopic,
            onTopicSelected = { topic ->
                selectedTopic = topic.id ?: 0
                scope.launch { gridState.scrollToItem(0) }
            },
            isSearchBarVisible = isSearchBarVisible,
            hazeState = hazeState,
            layoutStyle = layoutStyle,
            onLayoutToggle = {
                layoutStyle = if (layoutStyle == HomeLayoutStyle.Grid) {
                    HomeLayoutStyle.Feed
                } else {
                    HomeLayoutStyle.Grid
                }

                scope.launch { gridState.scrollToItem(0) }
            }
        )
    }
}

@OptIn(ExperimentalHazeMaterialsApi::class)
@Composable
private fun HomeHeader(
    modifier: Modifier = Modifier,
    topics: List<Topic>,
    selectedTopic: Int,
    onTopicSelected: (Topic) -> Unit,
    isSearchBarVisible: Boolean,
    hazeState: HazeState,
    layoutStyle: HomeLayoutStyle,
    onLayoutToggle: () -> Unit,
) {
    val headerTransition = updateTransition(
        targetState = isSearchBarVisible,
        label = "home_header_transition"
    )
    val topicTranslationY by headerTransition.animateFloat(
        transitionSpec = { tween(durationMillis = 280) },
        label = "topic_list_slide"
    ) { visible -> if (visible) 0f else -12f }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(vertical = 12.dp)
    ) {
        AnimatedVisibility(
            visible = isSearchBarVisible,
            enter = fadeIn(animationSpec = tween(280)) +
                    expandVertically(animationSpec = tween(280), expandFrom = Alignment.Top),
            exit = fadeOut(animationSpec = tween(220)) +
                    shrinkVertically(animationSpec = tween(220), shrinkTowards = Alignment.Top)
        ) {
            Column {
                HomeSearchBar(
                    hazeState = hazeState,
                    modifier = Modifier.padding(horizontal = 16.dp),
                    layoutStyle = layoutStyle,
                    onLayoutToggle = onLayoutToggle
                )
                Spacer(modifier = Modifier.height(12.dp))
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .graphicsLayer { translationY = topicTranslationY }
                .padding(start = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(end = 16.dp)
            ) {
                items(topics) { topic ->
                    val isSelected = topic.id == selectedTopic
                    Box(
                        modifier = Modifier
                            .widthIn(min = 80.dp)
                            .clip(RoundedCornerShape(50))
                            .hazeEffect(state = hazeState, style = CupertinoMaterials.ultraThin())
                            .background(
                                color = if (isSelected) Color(0xFF7B4FBF).copy(alpha = 0.85f)
                                else Color.White.copy(alpha = 0.25f)
                            )
                            .noRippleClickable { onTopicSelected(topic) }
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = topic.name!!,
                            color = if (isSelected) Color.White else Color.Black.copy(alpha = 0.6f),
                            fontSize = 13.sp,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                        )
                    }
                }

                item {
                    Box(
                        modifier = Modifier
                            .widthIn(min = 80.dp)
                            .clip(RoundedCornerShape(50))
                            .hazeEffect(state = hazeState, style = CupertinoMaterials.ultraThin())
                            .background(Color.White.copy(alpha = 0.2f))
                            .noRippleClickable { }
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Xem thêm",
                            color = Color(0xFF7B4FBF),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalHazeMaterialsApi::class)
@Composable
private fun LayoutToggleButton(
    layoutStyle: HomeLayoutStyle,
    hazeState: HazeState,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .hazeEffect(state = hazeState, style = CupertinoMaterials.ultraThin())
            .background(Color.White.copy(alpha = 0.2f))
            .noRippleClickable { onClick() }
            .padding(vertical = 4.dp, horizontal = 4.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(if (layoutStyle == HomeLayoutStyle.Grid) Color.White else Color.Transparent)
                    .padding(horizontal = 10.dp, vertical = 9.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.GridView,
                    contentDescription = "Grid layout",
                    tint = if (layoutStyle == HomeLayoutStyle.Grid) Color(0xFF7B4FBF) else Color.Black.copy(
                        alpha = 0.4f
                    ),
                    modifier = Modifier.size(16.dp)
                )
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(if (layoutStyle == HomeLayoutStyle.Feed) Color.White else Color.Transparent)
                    .padding(horizontal = 10.dp, vertical = 9.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.ViewAgenda,
                    contentDescription = "Feed layout",
                    tint = if (layoutStyle == HomeLayoutStyle.Feed) Color(0xFF7B4FBF) else Color.Black.copy(
                        alpha = 0.4f
                    ),
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalHazeMaterialsApi::class)
@Composable
private fun HomeSearchBar(
    hazeState: HazeState,
    modifier: Modifier = Modifier,
    layoutStyle: HomeLayoutStyle,
    onSearchClick: () -> Unit = {},
    onLayoutToggle: () -> Unit = {}
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(50))
                .hazeEffect(state = hazeState, style = CupertinoMaterials.ultraThin())
                .background(Color.White.copy(alpha = 0.25f))
                .noRippleClickable { onSearchClick() }
                .padding(horizontal = 16.dp, vertical = 13.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.Search,
                contentDescription = null,
                tint = Color.Black.copy(alpha = 0.6f),
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = "Khám phá nghệ thuật...",
                color = Color.Black.copy(alpha = 0.6f),
                fontSize = 14.sp,
                fontWeight = FontWeight.Normal
            )
        }

        LayoutToggleButton(
            layoutStyle = layoutStyle,
            hazeState = hazeState,
            onClick = onLayoutToggle
        )
    }
}

@Composable
fun CollectionCard(collection: Collection, aspectRatio: Float) {
    var isLiked by remember(collection.uuid) { mutableStateOf(collection.isLiked ?: false) }
    var isImageLoaded by remember(collection.uuid) { mutableStateOf(false) }
    val hasLoadErrorState = remember(collection.uuid) { mutableStateOf(false) }
    val firstImage = collection.images?.firstOrNull()
    val thumbnailUrl = firstImage?.urlSmall ?: firstImage?.urlRegular ?: firstImage?.urlFull

    Box(
        modifier = Modifier
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
            AsyncImage(
                model = thumbnailUrl,
                contentDescription = collection.uuid,
                contentScale = ContentScale.Crop,
                onLoading = { isImageLoaded = false },
                onSuccess = { isImageLoaded = true },
                onError = { isImageLoaded = true; hasLoadErrorState.value = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
            )
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(8.dp)
                .size(36.dp)
                .background(Color.White.copy(alpha = 0.85f), CircleShape)
                .noRippleClickable { isLiked = !isLiked },
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

@Composable
fun CollectionFeedCard(collection: Collection) {
    var isLiked by remember(collection.uuid) { mutableStateOf(collection.isLiked ?: false) }
    val images = collection.images.orEmpty()
    val displayImages = images.take(3)
    val totalImages = images.size
    val hasMore = totalImages > 3
    val pageCount = maxOf(1, if (hasMore) displayImages.size + 1 else displayImages.size)
    val pagerState = rememberPagerState(pageCount = { pageCount })
    val showAllBgImage = images.getOrNull(3) ?: images.getOrNull(2)
    val isOnShowAllPage = hasMore && pagerState.currentPage == displayImages.size
    val showAllRatio = ImageHelper.aspectRatio(showAllBgImage?.width, showAllBgImage?.height)

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
                            .aspectRatio(showAllRatio),
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
                                .noRippleClickable { }
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
                    val imageUrl = image?.urlSmall ?: image?.urlRegular ?: image?.urlFull
                    val imageRatio = ImageHelper.aspectRatio(image?.width, image?.height)
                    var isLoaded by remember(collection.uuid, page) {
                        mutableStateOf(imageLoadedStates.getOrElse(page) { false })
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(imageRatio)
                    ) {
                        if (!isLoaded) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .skeletonEffect()
                            )
                        }

                        if (imageUrl != null) {
                            AsyncImage(
                                model = imageUrl,
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
                                modifier = Modifier.fillMaxSize()
                            )
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
                modifier = Modifier.noRippleClickable { }
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

private fun ensureAllTopic(topics: List<Topic>): List<Topic> {
    val allTopic =
        topics.firstOrNull { it.id == 0 || it.name.equals(HOME_TOPICS_ALL, ignoreCase = true) }
            ?: Topic(id = 0, name = HOME_TOPICS_ALL)
    val filtered =
        topics.filterNot { it.id == 0 || it.name.equals(HOME_TOPICS_ALL, ignoreCase = true) }
    return listOf(allTopic) + filtered
}

