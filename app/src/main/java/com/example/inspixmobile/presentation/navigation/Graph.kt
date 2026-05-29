package com.example.inspixmobile.presentation.navigation

import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.LocalNavAnimatedContentScope
import androidx.navigation3.ui.NavDisplay
import com.example.inspixmobile.presentation.component.CommentSheetComponent
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource
import com.example.inspixmobile.presentation.component.NavigationBar
import com.example.inspixmobile.presentation.component.NavigationBarStyle
import com.example.inspixmobile.presentation.component.TopShadowOverlay
import com.example.inspixmobile.presentation.screen.DetailCollectionScreen
import com.example.inspixmobile.presentation.screen.DetailTopicScreen
import com.example.inspixmobile.presentation.screen.HomeScreen
import com.example.inspixmobile.presentation.screen.SearchResultScreen
import com.example.inspixmobile.presentation.screen.SearchScreen
import com.example.inspixmobile.presentation.state.rememberNavigationState
import com.example.inspixmobile.presentation.state.toEntries
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

@Composable
fun Graph() {
    val scope = rememberCoroutineScope()

    val navigationBarStyle = NavigationBarStyle.Float
    val topLevelRoutes by remember(navigationBarStyle) {
        derivedStateOf { topLevelRoutesFor(navigationBarStyle) }
    }
    val topLevelNavItems by remember(navigationBarStyle) {
        derivedStateOf { topLevelNavItemsFor(navigationBarStyle) }
    }
    val scrollToTopSignals = remember { mutableStateMapOf<NavKey, Int>() }
    val navigationState = rememberNavigationState(
        startRoute = Destination.Home,
        topLevelRoutes = ALL_TOP_LEVEL_ROUTES
    )
    val navigator = remember { Navigator(navigationState) }
    val navInsetBottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    val currentRoute by remember(navigationState) {
        derivedStateOf {
            navigationState.backStacks[navigationState.topLevelRoute]?.lastOrNull()
                ?: navigationState.topLevelRoute
        }
    }
    val isDetailCollectionRoute by remember(currentRoute) {
        derivedStateOf { currentRoute is Destination.DetailCollection }
    }
    val isNavBarVisible by remember(isDetailCollectionRoute) {
        derivedStateOf { !isDetailCollectionRoute }
    }

    val homeScrollSignal = scrollToTopSignals[Destination.Home] ?: 0
    val searchScrollSignal = scrollToTopSignals[Destination.Search] ?: 0

    val dockedBarHeight = 60.dp
    val bottomContentPadding = dockedBarHeight + navInsetBottom + 36.dp

    val pagerState = rememberPagerState(
        initialPage = navigationState.topLevelRoute.toTopLevelPageIndex(topLevelRoutes) ?: 0,
        pageCount = { topLevelRoutes.size }
    )
    val isUserScrollEnabled by remember(isDetailCollectionRoute) {
        derivedStateOf { !isDetailCollectionRoute }
    }

    val hazeState = remember { HazeState() }

    LaunchedEffect(navigationState.topLevelRoute, topLevelRoutes) {
        val targetPage = navigationState.topLevelRoute.toTopLevelPageIndex(topLevelRoutes) ?: 0
        if (pagerState.currentPage != targetPage) {
            pagerState.animateScrollToPage(targetPage)
        }
    }

    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.settledPage }
            .distinctUntilChanged()
            .collect { page ->
                val route = topLevelRouteForPage(page, topLevelRoutes)
                if (navigationState.topLevelRoute != route) {
                    navigator.switchTab(route)
                }
            }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxSize()
                .hazeSource(state = hazeState),
            beyondViewportPageCount = 1,
            userScrollEnabled = isUserScrollEnabled
        ) { page ->
            val route = topLevelRouteForPage(page, topLevelRoutes)

            SharedTransitionLayout {
                NavDisplay(
                    onBack = navigator::goBack,
                    modifier = Modifier.fillMaxSize(),
                    entries = navigationState.toEntries(
                        topLevelRoute = route,
                        entryProvider = entryProvider {
                            entry<Destination.Home> {
                                HomeScreen(
                                    sharedTransitionScope = this@SharedTransitionLayout,
                                    animatedVisibilityScope = LocalNavAnimatedContentScope.current,
                                    bottomContentPadding = bottomContentPadding,
                                    scrollToTopSignal = homeScrollSignal,
                                    navigateToDetailCollection = { collection ->
                                        navigator.push(Destination.DetailCollection(collection))
                                    },
                                    navigateToSearch = {
                                        navigator.switchTab(Destination.Search)
                                    }
                                )
                            }
                            entry<Destination.DetailCollection> { entry ->
                                val collection = entry.collection
                                DetailCollectionScreen(
                                    collection = collection,
                                    sharedTransitionScope = this@SharedTransitionLayout,
                                    animatedVisibilityScope = LocalNavAnimatedContentScope.current,
                                    bottomContentPadding = bottomContentPadding,
                                    navigateToDetailCollection = { collection ->
                                        navigator.push(Destination.DetailCollection(collection))
                                    },
                                    navigateBack = { navigator.goBack() }
                                )
                            }
                            entry<Destination.Search> {
                                SearchScreen(
                                    sharedTransitionScope = this@SharedTransitionLayout,
                                    animatedVisibilityScope = LocalNavAnimatedContentScope.current,
                                    bottomContentPadding = bottomContentPadding,
                                    scrollToTopSignal = searchScrollSignal,
                                    navigateToDetailTopic = { topic ->
                                        navigator.push(Destination.DetailTopic(topic))
                                    },
                                    navigateToSearchResult = { query ->
                                        navigator.push(Destination.SearchResult(query))
                                    }
                                )
                            }
                            entry<Destination.SearchResult> { entry ->
                                val query = entry.query
                                SearchResultScreen(
                                    query = query,
                                    sharedTransitionScope = this@SharedTransitionLayout,
                                    animatedVisibilityScope = LocalNavAnimatedContentScope.current,
                                    bottomContentPadding = bottomContentPadding,
                                    navigateToDetailCollection = { collection ->
                                        navigator.push(Destination.DetailCollection(collection))
                                    },
                                    navigateBack = { navigator.goBack() }
                                )
                            }
                            entry<Destination.Upload> {

                            }
                            entry<Destination.Followed> {

                            }
                            entry<Destination.Profile> {

                            }
                            entry<Destination.DetailTopic> { entry ->
                                val topic = entry.topic
                                DetailTopicScreen(
                                    topic = topic,
                                    sharedTransitionScope = this@SharedTransitionLayout,
                                    animatedVisibilityScope = LocalNavAnimatedContentScope.current,
                                    bottomContentPadding = bottomContentPadding,
                                    navigateBack = { navigator.goBack() },
                                    navigateToDetailCollection = { collection ->
                                        navigator.push(Destination.DetailCollection(collection))
                                    },
                                )
                            }
                        }
                    )
                )
            }
        }

        TopShadowOverlay()

        CommentSheetComponent()

        NavigationBar(
            modifier = Modifier.align(Alignment.BottomCenter),
            isVisible = isNavBarVisible,
            selectedKey = navigationState.topLevelRoute,
            onSelectKey = { route ->
                val stack = navigationState.backStacks[route]
                if (stack != null) {
                    val isSameTab = route == navigationState.topLevelRoute
                    if (stack.size > 1) {
                        while (stack.size > 1) {
                            stack.removeLastOrNull()
                        }
                    } else if (isSameTab) {
                        scrollToTopSignals[route] = (scrollToTopSignals[route] ?: 0) + 1
                    }
                }
                navigator.switchTab(route)
                val targetPage = route.toTopLevelPageIndex(topLevelRoutes) ?: return@NavigationBar
                scope.launch {
                    pagerState.animateScrollToPage(targetPage)
                }
            },
            items = topLevelNavItems,
            hazeState = hazeState,
            style = navigationBarStyle
        )
    }
}