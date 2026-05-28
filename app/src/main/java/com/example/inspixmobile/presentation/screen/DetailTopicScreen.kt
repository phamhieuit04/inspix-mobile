package com.example.inspixmobile.presentation.screen

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.EnterExitState
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
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.paging.compose.collectAsLazyPagingItems
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Bold
import com.adamglin.phosphoricons.bold.ArrowLeft
import com.example.inspixmobile.domain.model.Topic
import com.example.inspixmobile.presentation.component.TopShadowOverlay
import com.example.inspixmobile.presentation.component.TopicCardComponent
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

    val collections = searchViewModel.collections.collectAsLazyPagingItems()

    LaunchedEffect(topic.id) {
        searchViewModel.searchByTopic(topic.id!!)
    }

    LaunchedEffect(showOverlayRaw) {
        if (showOverlayRaw) {
            showOverlayDelayed = true
        } else {
            showOverlayDelayed = false
        }
    }

    BackHandler { navigateBack() }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(color = Color(0xFFF0F0F5))
    ) {
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
                    sharedTransitionScope = sharedTransitionScope,
                    animatedVisibilityScope = animatedVisibilityScope,
                    onClick = { }
                )
            }
        }

        TopShadowOverlay(
            height = 40.dp
        )

        with(sharedTransitionScope) {
            AnimatedVisibility(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .renderInSharedTransitionScopeOverlay(zIndexInOverlay = 1f),
                visible = showOverlayDelayed,
                enter = EnterTransition.None,
                exit = ExitTransition.None
            ) {
                Box(
                    modifier = Modifier
                        .statusBarsPadding()
                        .padding(top = 12.dp, start = 20.dp)
                        .background(color = backgroundColor, shape = CircleShape)
                        .clip(CircleShape)
                        .clickable(onClick = navigateBack)
                        .padding(14.dp)
                ) {
                    Icon(
                        imageVector = PhosphorIcons.Bold.ArrowLeft,
                        contentDescription = "Back",
                        tint = iconColor
                    )
                }
            }
        }
    }
}