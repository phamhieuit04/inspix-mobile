package com.example.inspixmobile.presentation.navigation

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
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource
import com.example.inspixmobile.presentation.component.NavigationBar
import com.example.inspixmobile.presentation.component.NavigationBarStyle
import com.example.inspixmobile.presentation.screen.HomeScreen
import com.example.inspixmobile.presentation.state.rememberNavigationState
import com.example.inspixmobile.presentation.state.toEntries
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

@Composable
fun Graph() {
    val navigationBarStyle = NavigationBarStyle.Float
    val topLevelRoutes by remember(navigationBarStyle) {
        derivedStateOf { topLevelRoutesFor(navigationBarStyle) }
    }
    val topLevelNavItems by remember(navigationBarStyle) {
        derivedStateOf { topLevelNavItemsFor(navigationBarStyle) }
    }

    val navigationState = rememberNavigationState(
        startRoute = Destination.Home,
        topLevelRoutes = ALL_TOP_LEVEL_ROUTES
    )

    val navigator = remember { Navigator(navigationState) }
    val hazeState = remember { HazeState() }
    val navInsetBottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    val dockedBarHeight = 60.dp
    val bottomContentPadding = dockedBarHeight + navInsetBottom + 36.dp
    val pagerState = rememberPagerState(
        initialPage = navigationState.topLevelRoute.toTopLevelPageIndex(topLevelRoutes) ?: 0,
        pageCount = { topLevelRoutes.size }
    )
    val scope = androidx.compose.runtime.rememberCoroutineScope()
    val appEntryProvider: (NavKey) -> NavEntry<NavKey> = remember {
        entryProvider {
            entry<Destination.Home> {
                HomeScreen(bottomContentPadding = bottomContentPadding)
            }
            entry<Destination.Search> {

            }
            entry<Destination.Upload> {

            }
            entry<Destination.Followed> {

            }
            entry<Destination.Profile> {

            }
        }
    }

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
                .hazeSource(state = hazeState)
        ) { page ->
            val route = topLevelRouteForPage(page, topLevelRoutes)
            NavDisplay(
                onBack = navigator::goBack,
                modifier = Modifier.fillMaxSize(),
                entries = navigationState.toEntries(
                    topLevelRoute = route,
                    entryProvider = appEntryProvider
                )
            )
        }

        NavigationBar(
            modifier = Modifier.align(Alignment.BottomCenter),
            selectedKey = navigationState.topLevelRoute,
            onSelectKey = { route ->
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