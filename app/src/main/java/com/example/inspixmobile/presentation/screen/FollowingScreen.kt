package com.example.inspixmobile.presentation.screen

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import com.example.inspixmobile.domain.model.Collection
import com.example.inspixmobile.presentation.component.CollectionFeedCardComponent
import com.example.inspixmobile.presentation.component.EmptyCollectionsComponent
import com.example.inspixmobile.presentation.component.ShimmerFeedItem
import com.example.inspixmobile.presentation.viewmodel.CommentSheetViewModel
import com.example.inspixmobile.presentation.viewmodel.FollowingViewModel
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun FollowingScreen(
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    bottomContentPadding: Dp = 8.dp,
    scrollToTopSignal: Int,
    navigateToDetailCollection: (Collection) -> Unit,
    followingViewModel: FollowingViewModel = koinViewModel(),
    commentSheetViewModel: CommentSheetViewModel = koinViewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val statusBarPadding =
        WindowInsets.statusBars.asPaddingValues().calculateTopPadding()

    val pagingCollections =
        followingViewModel.followedCollections.collectAsLazyPagingItems()

    val interactions by followingViewModel.interactions.collectAsState()

    val pullToRefreshState = rememberPullToRefreshState()
    val lazyListState = rememberLazyListState()

    val isRefreshing = pagingCollections.loadState.refresh is LoadState.Loading
    val isRefreshError = pagingCollections.loadState.refresh is LoadState.Error
    val isLoadFinished = pagingCollections.loadState.refresh is LoadState.NotLoading
    val isEmpty = isRefreshError || (isLoadFinished && pagingCollections.itemCount == 0)

    var isUserRefreshing by remember { mutableStateOf(false) }
    val isShimmering = isRefreshing && (pagingCollections.itemCount == 0 || isUserRefreshing)

    var lastScrollToTopSignal by rememberSaveable { mutableIntStateOf(0) }

    LaunchedEffect(scrollToTopSignal) {
        if (scrollToTopSignal > lastScrollToTopSignal) {
            lazyListState.scrollToItem(0)
            lastScrollToTopSignal = scrollToTopSignal
        }
    }

    LaunchedEffect(isRefreshing) {
        if (!isRefreshing) {
            isUserRefreshing = false
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF0F0F5))
    ) {
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = {
                isUserRefreshing = true
                scope.launch {
                    lazyListState.scrollToItem(0)
                    pagingCollections.refresh()
                }
            },
            state = pullToRefreshState,
            indicator = {
                PullToRefreshDefaults.Indicator(
                    state = pullToRefreshState,
                    isRefreshing = isRefreshing,
                    modifier = Modifier.align(Alignment.TopCenter)
                )
            },
            modifier = Modifier.fillMaxSize()
        ) {
            AnimatedContent(
                targetState = isShimmering,
                transitionSpec = {
                    fadeIn(animationSpec = tween(durationMillis = 400)) togetherWith
                            fadeOut(animationSpec = tween(durationMillis = 300))
                },
                label = "following_layout_transition"
            ) { currentShimmering ->
                when {
                    isEmpty -> {
                        EmptyCollectionsComponent(
                            modifier = Modifier.fillMaxSize(),
                            descriptionText = if (isRefreshError)
                                "Vui lòng kiểm tra kết nối mạng và thử lại."
                            else
                                "Theo dõi nghệ sĩ để khám phá thêm nha.",
                            onRetry = {
                                isUserRefreshing = true
                                scope.launch {
                                    lazyListState.scrollToItem(0)
                                    if (isRefreshError) pagingCollections.retry()
                                    else pagingCollections.refresh()
                                }
                            }
                        )
                    }

                    currentShimmering -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(top = statusBarPadding),
                            userScrollEnabled = false
                        ) {
                            items(6) {
                                ShimmerFeedItem()
                            }
                        }
                    }

                    else -> {
                        LazyColumn(
                            state = lazyListState,
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(
                                top = statusBarPadding,
                                bottom = bottomContentPadding + 16.dp
                            ),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(
                                count = pagingCollections.itemCount,
                                key = { index ->
                                    pagingCollections[index]?.uuid ?: index
                                }
                            ) { index ->
                                val collection = pagingCollections[index]

                                if (collection != null) {
                                    val interaction = interactions[collection.uuid]

                                    val isLiked =
                                        interaction?.isLiked ?: (collection.isLiked ?: false)

                                    val totalLikes =
                                        interaction?.totalLikes ?: collection.totalLikes

                                    val totalComments =
                                        interaction?.totalComments ?: collection.totalComments

                                    CollectionFeedCardComponent(
                                        context = context,
                                        collection = collection,
                                        isLiked = isLiked,
                                        totalLikes = totalLikes ?: 0,
                                        totalComments = totalComments ?: 0,
                                        onClick = {
                                            navigateToDetailCollection(collection)
                                        },
                                        onToggleLike = {
                                            followingViewModel.toggleLike(collection)
                                        },
                                        onShowComments = {
                                            commentSheetViewModel.show(collection.uuid!!)
                                        },
                                        sharedTransitionScope = sharedTransitionScope,
                                        animatedVisibilityScope = animatedVisibilityScope
                                    )
                                }
                            }

                            if (pagingCollections.loadState.append is LoadState.Loading) {
                                item {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        CircularProgressIndicator()
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}