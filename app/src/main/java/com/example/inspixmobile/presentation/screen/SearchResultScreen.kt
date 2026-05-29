package com.example.inspixmobile.presentation.screen

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Bold
import com.adamglin.phosphoricons.bold.ArrowLeft
import com.example.inspixmobile.core.util.ImageHelper
import com.example.inspixmobile.domain.model.Collection
import com.example.inspixmobile.presentation.component.CollectionCardComponent
import com.example.inspixmobile.presentation.component.EmptyCollectionsComponent
import com.example.inspixmobile.presentation.component.ShimmerGridItem
import com.example.inspixmobile.presentation.component.TopShadowOverlay
import com.example.inspixmobile.presentation.viewmodel.SearchViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun SearchResultScreen(
    modifier: Modifier = Modifier,
    query: String,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    bottomContentPadding: Dp = 8.dp,
    onUserScrollChanged: (Boolean) -> Unit,
    onNavBarVisibleChanged: (Boolean) -> Unit,
    navigateToDetailCollection: (Collection) -> Unit,
    navigateBack: () -> Unit,
    searchViewModel: SearchViewModel = koinViewModel()
) {
    val scope = rememberCoroutineScope()
    val density = LocalDensity.current
    val context = LocalContext.current

    val backgroundColor = Color(0xFFe8e8e9)
    val iconColor = Color.DarkGray

    val statusBarPadding = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()

    val collectionsFlow = remember(query) {
        searchViewModel.getCollectionsPagingByQuery(query)
    }
    val collections = collectionsFlow.collectAsLazyPagingItems()
    val collectionsLoading =
        collections.loadState.refresh is LoadState.Loading && collections.itemCount == 0

    LaunchedEffect(Unit) {
        scope.launch {
            onUserScrollChanged(true)

            delay(220)
            onNavBarVisibleChanged(true)
        }
    }

    BackHandler { navigateBack() }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(color = Color(0xFFF0F0F5))
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

        TopShadowOverlay()

        Box(
            modifier = Modifier
                .statusBarsPadding()
                .padding(top = 12.dp, start = 20.dp)
                .background(color = backgroundColor, shape = CircleShape)
                .clip(CircleShape)
                .clickable(onClick = navigateBack)
                .padding(14.dp)
                .align(Alignment.TopStart)
        ) {
            Icon(
                imageVector = PhosphorIcons.Bold.ArrowLeft,
                contentDescription = "Back",
                tint = iconColor
            )
        }
    }
}
