package com.example.inspixmobile.presentation.screen

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.EnterExitState
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import com.example.inspixmobile.core.extension.noRippleClickable
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
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.inspixmobile.core.util.ImageHelper
import com.example.inspixmobile.domain.model.Topic
import com.example.inspixmobile.presentation.component.CollectionCardComponent
import com.example.inspixmobile.presentation.component.CollectionFeedCardComponent
import com.example.inspixmobile.presentation.component.EmptyCollectionsComponent
import com.example.inspixmobile.presentation.component.ShimmerFeedItem
import com.example.inspixmobile.presentation.component.ShimmerGridItem
import com.example.inspixmobile.presentation.viewmodel.CommentSheetViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel

enum class HomeLayoutStyle { Grid, Feed }

private const val HOME_TOPICS_ALL = "Tất cả"

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    bottomContentPadding: Dp = 8.dp,
    scrollToTopSignal: Int,
    navigateToDetailCollection: (Collection) -> Unit,
    navigateToSearch: () -> Unit,
    homeViewModel: HomeViewModel = koinViewModel(),
    commentSheetViewModel: CommentSheetViewModel = koinViewModel()
) {
    val density = LocalDensity.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val hazeState = rememberHazeState()
    val gridState = rememberLazyStaggeredGridState()
    val pullToRefreshState = rememberPullToRefreshState()

    val selectedTopic by homeViewModel.selectedTopic.collectAsStateWithLifecycle()
    val topics by homeViewModel.topics.collectAsStateWithLifecycle()
    val displayTopics = remember(topics) { ensureAllTopic(topics) }

    val pagingCollections = homeViewModel.collections.collectAsLazyPagingItems()
    var layoutStyle by rememberSaveable { mutableStateOf(HomeLayoutStyle.Grid) }
    var headerHeightPx by remember { mutableIntStateOf(0) }
    val headerHeightDp = with(density) { headerHeightPx.toDp() }

    val showHeaderRaw by remember {
        derivedStateOf {
            animatedVisibilityScope.transition.targetState == EnterExitState.Visible
        }
    }
    var showHeaderDelayed by remember { mutableStateOf(false) }
    val headerAlpha by animateFloatAsState(
        targetValue = if (showHeaderDelayed) 1f else 0f,
        animationSpec = if (showHeaderDelayed) tween(220) else tween(0),
        label = "home_header_alpha"
    )

    var isSearchBarVisible by rememberSaveable { mutableStateOf(true) }
    var lastScrollToTopSignal by rememberSaveable { mutableIntStateOf(0) }
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

    LaunchedEffect(scrollToTopSignal) {
        if (scrollToTopSignal > lastScrollToTopSignal) {
            gridState.animateScrollToItem(0)
            lastScrollToTopSignal = scrollToTopSignal
        }
    }

    LaunchedEffect(isRefreshing) {
        if (!isRefreshing) {
            userRefreshRequested = false
        }
    }

    LaunchedEffect(showHeaderRaw) {
        if (showHeaderRaw) {
            showHeaderDelayed = false
            delay(400)
            showHeaderDelayed = true
        } else {
            showHeaderDelayed = false
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
            val isLoadFinished = pagingCollections.loadState.refresh is LoadState.NotLoading
                    || pagingCollections.loadState.refresh is LoadState.Error
            val isEmpty = isLoadFinished && pagingCollections.itemCount == 0
            val isInitialLoading = pagingCollections.loadState.refresh is LoadState.Loading
                    && pagingCollections.itemCount == 0
                    && !userRefreshRequested

            AnimatedContent(
                targetState = isInitialLoading to layoutStyle,
                transitionSpec = {
                    fadeIn(animationSpec = tween(durationMillis = 400)) togetherWith
                            fadeOut(animationSpec = tween(durationMillis = 300))
                },
                label = "layout_transition",
                contentKey = { (loading, layout) -> "$loading-$layout" }
            ) { (currentLoading, currentLayout) ->
                if (isEmpty) {
                    EmptyCollectionsComponent(
                        modifier = Modifier
                            .fillMaxSize()
                            .hazeSource(state = hazeState),
                        onRetry = {
                            userRefreshRequested = true
                            scope.launch {
                                gridState.scrollToItem(0)
                                pagingCollections.refresh()
                            }
                        }
                    )
                } else {
                    if (currentLoading) {
                        LazyVerticalStaggeredGrid(
                            columns = when (currentLayout) {
                                HomeLayoutStyle.Grid -> StaggeredGridCells.Fixed(2)
                                HomeLayoutStyle.Feed -> StaggeredGridCells.Fixed(1)
                            },
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
                                .hazeSource(state = hazeState),
                            userScrollEnabled = false
                        ) {
                            items(count = 8) { index ->
                                when (currentLayout) {
                                    HomeLayoutStyle.Grid -> ShimmerGridItem(index = index)
                                    HomeLayoutStyle.Feed -> ShimmerFeedItem()
                                }
                            }
                        }
                    } else {
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
                                            CollectionCardComponent(
                                                context = context,
                                                sharedTransitionScope = sharedTransitionScope,
                                                animatedVisibilityScope = animatedVisibilityScope,
                                                collection = collection,
                                                aspectRatio = resolvedRatio,
                                                onClick = {
                                                    navigateToDetailCollection(collection)
                                                }
                                            )
                                        }
                                    }

                                    HomeLayoutStyle.Feed -> {
                                        if (collection != null) {
                                            CollectionFeedCardComponent(
                                                collection = collection,
                                                context = context,
                                                sharedTransitionScope = sharedTransitionScope,
                                                animatedVisibilityScope = animatedVisibilityScope,
                                                onClick = {
                                                    navigateToDetailCollection(collection)
                                                },
                                                onShowComments = {
                                                    commentSheetViewModel.show(it.uuid!!)
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        with(sharedTransitionScope) {
            HomeHeader(
                modifier = Modifier
                    .renderInSharedTransitionScopeOverlay(zIndexInOverlay = 1f)
                    .graphicsLayer { alpha = headerAlpha }
                    .onSizeChanged { headerHeightPx = it.height },
                topics = displayTopics.take(6),
                selectedTopic = selectedTopic,
                onTopicSelected = { topic ->
                    homeViewModel.selectTopic(topic.id ?: 0)
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
                },
                navigateToSearch = navigateToSearch
            )
        }
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
    navigateToSearch: () -> Unit
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
                    onLayoutToggle = onLayoutToggle,
                    onSearchClick = navigateToSearch
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

private fun ensureAllTopic(topics: List<Topic>): List<Topic> {
    val allTopic =
        topics.firstOrNull { it.id == 0 || it.name.equals(HOME_TOPICS_ALL, ignoreCase = true) }
            ?: Topic(id = 0, name = HOME_TOPICS_ALL)
    val filtered =
        topics.filterNot { it.id == 0 || it.name.equals(HOME_TOPICS_ALL, ignoreCase = true) }
    return listOf(allTopic) + filtered
}