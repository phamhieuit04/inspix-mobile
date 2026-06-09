package com.example.inspixmobile.presentation.screen

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.EnterExitState
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBars
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import com.example.inspixmobile.core.util.ImageHelper
import com.example.inspixmobile.domain.model.Collection
import com.example.inspixmobile.domain.model.Topic
import com.example.inspixmobile.presentation.component.BackScaffold
import com.example.inspixmobile.presentation.component.CollectionCardComponent
import com.example.inspixmobile.presentation.component.EmptyCollectionsComponent
import com.example.inspixmobile.presentation.component.ShimmerGridItem
import com.example.inspixmobile.presentation.component.TopicCardComponent
import com.example.inspixmobile.presentation.component.MasonryItemSpan
import com.example.inspixmobile.presentation.component.VerticalMasonryGrid
import com.example.inspixmobile.presentation.viewmodel.SearchViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun DetailTopicScreen(
    modifier: Modifier = Modifier,
    topic: Topic,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    bottomContentPadding: Dp = 8.dp,
    navigateBack: () -> Unit,
    navigateToDetailCollection: (Collection) -> Unit,
    searchViewModel: SearchViewModel = koinViewModel()
) {
    val context = LocalContext.current

    val showOverlayRaw by remember {
        derivedStateOf {
            animatedVisibilityScope.transition.targetState == EnterExitState.Visible
        }
    }
    var showOverlayDelayed by remember { mutableStateOf(true) }
    val statusBarPadding = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()

    val collectionsFlow = remember(topic.id) {
        searchViewModel.getCollectionsPagingByTopic(topic.id!!)
    }
    val collections = collectionsFlow.collectAsLazyPagingItems()
    val collectionsLoading =
        collections.loadState.refresh is LoadState.Loading && collections.itemCount == 0

    val interactions by searchViewModel.interactions.collectAsState()

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
        val isLoading = collectionsLoading
        val isError = collections.loadState.refresh is LoadState.Error

        if (isError) {
            EmptyCollectionsComponent(
                modifier = Modifier.fillMaxSize(),
                buttonText = "Quay lại",
                onRetry = navigateBack
            )
        } else {
            VerticalMasonryGrid(
                modifier = Modifier.fillMaxSize(),
                columns = 2,
                contentPadding = PaddingValues(
                    top = statusBarPadding,
                    start = 8.dp,
                    end = 8.dp,
                    bottom = bottomContentPadding + 16.dp
                ),
                horizontalItemSpacing = 8.dp,
                verticalItemSpacing = 8.dp,
            ) {
                item(
                    key = "topic-header-${topic.id}",
                    span = MasonryItemSpan.FullLine,
                    aspectRatio = 3f / 2f
                ) {
                    TopicCardComponent(
                        context = context,
                        topic = topic,
                        fontSize = 20.sp,
                        sharedTransitionScope = sharedTransitionScope,
                        animatedVisibilityScope = animatedVisibilityScope,
                        onClick = { }
                    )
                }

                if (isLoading) {
                    items(
                        count = 30,
                        key = { index -> "topic-shimmer-$index" },
                        aspectRatio = { index ->
                            if (index % 3 == 0) 0.75f else if (index % 3 == 1) 1.2f else 1.0f
                        }
                    ) { index ->
                        ShimmerGridItem(index = index)
                    }
                } else {
                    items(
                        count = collections.itemCount,
                        key = { index ->
                            collections.peek(index)?.uuid ?: "topic-collection-$index"
                        },
                        aspectRatio = { index ->
                            val collection = collections.peek(index)
                            val coverImage = collection?.images?.firstOrNull()
                            ImageHelper.aspectRatio(
                                coverImage?.width,
                                coverImage?.height
                            )
                        }
                    ) { index ->
                        val collection = collections[index]
                        if (collection != null) {
                            val coverImage = collection.images?.firstOrNull()
                            val resolvedRatio = ImageHelper.aspectRatio(
                                coverImage?.width,
                                coverImage?.height
                            )

                            val interaction = interactions[collection.uuid]
                            val isLiked =
                                interaction?.isLiked ?: (collection.isLiked
                                    ?: false)

                            CollectionCardComponent(
                                context = context,
                                sharedTransitionScope = sharedTransitionScope,
                                animatedVisibilityScope = animatedVisibilityScope,
                                collection = collection,
                                isLiked = isLiked,
                                aspectRatio = resolvedRatio,
                                onClick = {
                                    navigateToDetailCollection(collection)
                                },
                                onToggleLike = { searchViewModel.toggleLike(collection) }
                            )
                        }
                    }
                }
            }
        }
    }
}
