package com.example.inspixmobile.presentation.screen

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.EnterExitState
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
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
import com.example.inspixmobile.presentation.viewmodel.SearchViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun DetailTopicScreen(
    modifier: Modifier = Modifier,
    topic: Topic,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    bottomContentPadding: Dp = 8.dp,
    onUserScrollChanged: (Boolean) -> Unit,
    onNavBarVisibleChanged: (Boolean) -> Unit,
    navigateBack: () -> Unit,
    navigateToDetailCollection: (Collection) -> Unit,
    searchViewModel: SearchViewModel = koinViewModel()
) {
    val scope = rememberCoroutineScope()
    val density = LocalDensity.current
    val context = LocalContext.current

    val backgroundColor = Color(0xFFe8e8e9)
    val iconColor = Color.DarkGray

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

    LaunchedEffect(showOverlayRaw) {
        if (showOverlayRaw) {
            showOverlayDelayed = true
        } else {
            showOverlayDelayed = false
        }
    }

    LaunchedEffect(Unit) {
        scope.launch {
            onUserScrollChanged(true)

            delay(220)
            onNavBarVisibleChanged(true)
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
            LazyVerticalStaggeredGrid(
                modifier = Modifier.fillMaxSize(),
                columns = StaggeredGridCells.Fixed(2),
                contentPadding = PaddingValues(
                    top = statusBarPadding,
                    start = 8.dp,
                    end = 8.dp,
                    bottom = bottomContentPadding + 16.dp
                ),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalItemSpacing = 8.dp,
            ) {
                item(span = StaggeredGridItemSpan.FullLine) {
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
                    items(30) { index ->
                        ShimmerGridItem(index = index)
                    }
                } else {
                    items(count = collections.itemCount) { index ->
                        val collection = collections[index]
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
                                    scope.launch {
                                        navigateToDetailCollection(collection)
                                        onUserScrollChanged(false)

                                        delay(220)
                                        onNavBarVisibleChanged(false)
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}