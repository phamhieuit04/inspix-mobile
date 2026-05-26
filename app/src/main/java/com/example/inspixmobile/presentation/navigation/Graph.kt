package com.example.inspixmobile.presentation.navigation

import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.LocalNavAnimatedContentScope
import androidx.navigation3.ui.NavDisplay
import com.example.inspixmobile.presentation.component.CommentSheetComponent
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource
import com.example.inspixmobile.presentation.component.NavigationBar
import com.example.inspixmobile.presentation.component.NavigationBarStyle
import com.example.inspixmobile.presentation.screen.DetailCollectionScreen
import com.example.inspixmobile.presentation.screen.HomeScreen
import com.example.inspixmobile.presentation.screen.SearchScreen
import com.example.inspixmobile.presentation.state.rememberNavigationState
import com.example.inspixmobile.presentation.state.toEntries
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import android.util.Log
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.saveable.LocalSaveableStateRegistry

@Composable
fun Graph() {
    val scope = rememberCoroutineScope()

    val navigationBarStyle = NavigationBarStyle.Docked
    val topLevelRoutes by remember(navigationBarStyle) {
        derivedStateOf { topLevelRoutesFor(navigationBarStyle) }
    }
    val topLevelNavItems by remember(navigationBarStyle) {
        derivedStateOf { topLevelNavItemsFor(navigationBarStyle) }
    }
    val navigationState = rememberNavigationState(
        startRoute = Destination.Home,
        topLevelRoutes = ALL_TOP_LEVEL_ROUTES,
        restoreTopLevelRoute = false
    )
    val navigator = remember { Navigator(navigationState) }
    val navInsetBottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    var isNavBarVisible by remember { mutableStateOf(true) }

    val dockedBarHeight = 60.dp
    val bottomContentPadding = dockedBarHeight + navInsetBottom + 36.dp
    val homePageIndex = Destination.Home.toTopLevelPageIndex(topLevelRoutes) ?: 0

    val hazeState = remember { HazeState() }

    CompositionLocalProvider(LocalSaveableStateRegistry provides null) {
        val pagerState = rememberPagerState(
            initialPage = homePageIndex,
            pageCount = { topLevelRoutes.size }
        )
        var isUserScrollEnabled by remember { mutableStateOf(true) }
        var allowPagerSync by remember { mutableStateOf(false) }
        var allowPagerToNav by remember { mutableStateOf(false) }
        var allowRouteChanges by remember { mutableStateOf(false) }

        LaunchedEffect(topLevelRoutes) {
            if (pagerState.currentPage != homePageIndex) {
                Log.d("Graph", "Startup: force pager to Home page")
                pagerState.scrollToPage(homePageIndex)
            }
            Log.d("Graph", "Startup: currentPage=${pagerState.currentPage}, homePage=$homePageIndex")
            snapshotFlow { pagerState.currentPage }
                .first { it == homePageIndex }
            if (navigationState.topLevelRoute != Destination.Home) {
                Log.d("Graph", "Startup: force topLevelRoute=Home")
                navigationState.topLevelRoute = Destination.Home
            }
            allowPagerSync = true
        }

        LaunchedEffect(navigationState, allowRouteChanges) {
            snapshotFlow { navigationState.topLevelRoute }
                .distinctUntilChanged()
                .collect { route ->
                    Log.d("Graph", "Route changed: $route")
                    if (!allowRouteChanges && route != Destination.Home) {
                        Log.d("Graph", "Startup guard: reset to Home")
                        navigationState.topLevelRoute = Destination.Home
                        pagerState.scrollToPage(homePageIndex)
                    }
                }
        }

        LaunchedEffect(pagerState, allowRouteChanges) {
            snapshotFlow { pagerState.settledPage }
                .distinctUntilChanged()
                .collect { page ->
                    if (!allowRouteChanges && page != homePageIndex) {
                        Log.d("Graph", "Startup guard: reset pager to Home (page=$page)")
                        pagerState.scrollToPage(homePageIndex)
                    }
                }
        }

        LaunchedEffect(navigationState.topLevelRoute, topLevelRoutes, allowPagerSync) {
            if (!allowPagerSync) return@LaunchedEffect
            val targetPage = navigationState.topLevelRoute.toTopLevelPageIndex(topLevelRoutes) ?: 0
            if (pagerState.currentPage != targetPage) {
                Log.d("Graph", "Nav->Pager: ${navigationState.topLevelRoute}")
                pagerState.animateScrollToPage(targetPage)
            }
        }

        LaunchedEffect(pagerState, allowPagerSync, allowPagerToNav) {
            if (!allowPagerSync || !allowPagerToNav) return@LaunchedEffect
            snapshotFlow { pagerState.settledPage }
                .distinctUntilChanged()
                .collect { page ->
                    val route = topLevelRouteForPage(page, topLevelRoutes)
                    if (navigationState.topLevelRoute != route) {
                        Log.d("Graph", "Pager->Nav: $route")
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
                    .hazeSource(state = hazeState)
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDragStart = {
                                if (!allowPagerToNav) {
                                    Log.d("Graph", "Pager: user drag detected")
                                    allowPagerToNav = true
                                }
                                if (!allowRouteChanges) {
                                    allowRouteChanges = true
                                }
                            }
                        ) { _, _ -> }
                    },
                beyondViewportPageCount = 5,
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
                                        onUserScrollChanged = { isUserScrollEnabled = it },
                                        onNavBarVisibleChanged = { isNavBarVisible = it },
                                        navigateToDetailCollection = { collection ->
                                            navigator.push(Destination.DetailCollection(collection))
                                        },
                                        navigateToSearch = {
                                            allowRouteChanges = true
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
                                    SearchScreen()
                                }
                                entry<Destination.Upload> {

                                }
                                entry<Destination.Followed> {

                                }
                                entry<Destination.Profile> {

                                }
                            }
                        )
                    )
                }
            }

            CommentSheetComponent()

            NavigationBar(
                modifier = Modifier.align(Alignment.BottomCenter),
                isVisible = isNavBarVisible,
                selectedKey = navigationState.topLevelRoute,
                onSelectKey = { route ->
                    allowRouteChanges = true
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
}