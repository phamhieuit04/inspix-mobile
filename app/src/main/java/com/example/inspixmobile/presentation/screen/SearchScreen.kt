package com.example.inspixmobile.presentation.screen

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.inspixmobile.domain.model.Topic
import com.example.inspixmobile.presentation.component.BlurSearchBarComponent
import com.example.inspixmobile.presentation.component.TopicCardComponent
import com.example.inspixmobile.presentation.viewmodel.SearchViewModel
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.materials.CupertinoMaterials
import dev.chrisbanes.haze.materials.ExperimentalHazeMaterialsApi
import dev.chrisbanes.haze.rememberHazeState
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalHazeMaterialsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    modifier: Modifier = Modifier,
    isTablet: Boolean,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    bottomContentPadding: Dp = 8.dp,
    scrollToTopSignal: Int,
    navigateToDetailTopic: (Topic) -> Unit,
    navigateToSearchResult: (String) -> Unit,
    searchViewModel: SearchViewModel = koinViewModel()
) {
    val density = LocalDensity.current
    val context = LocalContext.current
    val softwareKeyboardController = LocalSoftwareKeyboardController.current

    val topics by searchViewModel.topics.collectAsStateWithLifecycle()
    val firstTopic = topics.firstOrNull()

    var query by remember { mutableStateOf("") }
    var lastScrollToTopSignal by rememberSaveable { mutableIntStateOf(0) }

    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current

    val hazeState = rememberHazeState()
    val hazeStyle = CupertinoMaterials.ultraThin()
    val gridState = rememberLazyGridState()

    var headerHeightPx by remember { mutableIntStateOf(0) }
    val headerHeightDp = with(density) { headerHeightPx.toDp() }

    LaunchedEffect(scrollToTopSignal) {
        if (scrollToTopSignal > lastScrollToTopSignal) {
            gridState.animateScrollToItem(0)
            lastScrollToTopSignal = scrollToTopSignal
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF0F0F5))
    ) {
        if (!isTablet) {
            LazyVerticalGrid(
                state = gridState,
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(
                    top = headerHeightDp,
                    start = 8.dp,
                    end = 8.dp,
                    bottom = bottomContentPadding + 16.dp
                ),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.hazeSource(hazeState)
            ) {
                item(span = {
                    GridItemSpan(maxLineSpan)
                }) {
                    TopicCardComponent(
                        context = context,
                        sharedTransitionScope = sharedTransitionScope,
                        animatedVisibilityScope = animatedVisibilityScope,
                        topic = firstTopic ?: return@item,
                        fontSize = 16.sp,
                        onClick = {
                            navigateToDetailTopic(firstTopic)
                        }
                    )
                }

                items(items = topics.drop(1)) { topic ->
                    TopicCardComponent(
                        context = context,
                        sharedTransitionScope = sharedTransitionScope,
                        animatedVisibilityScope = animatedVisibilityScope,
                        topic = topic,
                        onClick = {
                            navigateToDetailTopic(topic)
                        }
                    )
                }
            }
        } else {
            LazyVerticalGrid(
                state = gridState,
                columns = GridCells.Fixed(4),
                contentPadding = PaddingValues(
                    top = headerHeightDp,
                    start = 8.dp,
                    end = 8.dp,
                    bottom = bottomContentPadding + 16.dp
                ),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.hazeSource(hazeState)
            ) {
                items(items = topics) { topic ->
                    TopicCardComponent(
                        context = context,
                        sharedTransitionScope = sharedTransitionScope,
                        animatedVisibilityScope = animatedVisibilityScope,
                        topic = topic,
                        onClick = {
                            navigateToDetailTopic(topic)
                        }
                    )
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .onSizeChanged { headerHeightPx = it.height }
                .padding(16.dp)
                .statusBarsPadding(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            BlurSearchBarComponent(
                modifier = Modifier.widthIn(
                    max = if (isTablet) 600.dp
                    else Dp.Unspecified
                ),
                query = query,
                onQueryChange = { query = it },
                onSearch = {
                    focusManager.clearFocus()
                    navigateToSearchResult(query)
                },
                focusRequester = focusRequester,
                focusManager = focusManager,
                hazeState = hazeState,
                hazeStyle = hazeStyle
            )
        }
    }
}