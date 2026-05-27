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
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.example.inspixmobile.presentation.screen.ProfileScreen
import kotlinx.coroutines.flow.distinctUntilChanged

@Composable
fun Graph() {
    val navigationBarStyle = NavigationBarStyle.Float
    val navInsetBottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    val dockedBarHeight = 60.dp
    val bottomContentPadding = dockedBarHeight + navInsetBottom + 36.dp

    val topLevelRoutes = remember(navigationBarStyle) {
        val routes = if (navigationBarStyle == NavigationBarStyle.Float) {
            FLOATING_TOP_LEVEL_ROUTES
        } else {
            DOCKED_TOP_LEVEL_ROUTES
        }
        routes.map { it as Destination }
    }
    val navItems = if (navigationBarStyle == NavigationBarStyle.Float) {
        FLOATING_TOP_LEVEL_NAV_ITEMS
    } else {
        DOCKED_TOP_LEVEL_NAV_ITEMS
    }

    val homePageIndex = topLevelRoutes.indexOf(Destination.Home).coerceAtLeast(0)
    val backStack = remember { mutableStateListOf<Destination>(Destination.Home) }
    val selectedTopLevelRoute by remember {
        derivedStateOf { backStack.firstOrNull() ?: Destination.Home }
    }

    val pagerState = rememberPagerState(
        initialPage = homePageIndex,
        pageCount = { topLevelRoutes.size },
    )

    var isUserScrollEnabled by remember { mutableStateOf(true) }
    var isNavBarVisible by remember { mutableStateOf(true) }
    val isPagerScrollEnabled by remember {
        derivedStateOf { isUserScrollEnabled && backStack.size <= 1 }
    }

    val currentPage by remember { derivedStateOf { pagerState.currentPage } }

    LaunchedEffect(pagerState, topLevelRoutes) {
        snapshotFlow { pagerState.settledPage }
            .distinctUntilChanged()
            .collect { page ->
                val route = topLevelRoutes.getOrNull(page) ?: return@collect
                if (selectedTopLevelRoute != route) {
                    backStack.clear()
                    backStack.add(route)
                }
            }
    }

    LaunchedEffect(selectedTopLevelRoute, topLevelRoutes) {
        val pageIndex = topLevelRoutes.indexOf(selectedTopLevelRoute)
        if (pageIndex >= 0 && pagerState.currentPage != pageIndex) {
            pagerState.animateScrollToPage(pageIndex)
        }
    }

    val hazeState = remember { HazeState() }

    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxSize()
                .hazeSource(state = hazeState),
            beyondViewportPageCount = 5,
            userScrollEnabled = isPagerScrollEnabled
        ) { page ->
            val routeForPage = topLevelRoutes[page]
            val pageBackStack: List<Destination> = if (page == currentPage) {
                backStack
            } else {
                listOf(routeForPage)
            }

            SharedTransitionLayout {
                NavDisplay(
                    backStack = pageBackStack,
                    onBack = {
                        if (page == currentPage && backStack.size > 1) {
                            backStack.removeAt(backStack.lastIndex)
                        }
                    },
                    modifier = Modifier.fillMaxSize(),
                    entryProvider = entryProvider {
                        entry<Destination.Home> {
                            HomeScreen(
                                sharedTransitionScope = this@SharedTransitionLayout,
                                animatedVisibilityScope = LocalNavAnimatedContentScope.current,
                                bottomContentPadding = bottomContentPadding,
                                onUserScrollChanged = { isUserScrollEnabled = it },
                                onNavBarVisibleChanged = { isNavBarVisible = it },
                                navigateToDetailCollection = { collection ->
                                    if (page == currentPage) {
                                        backStack.add(Destination.DetailCollection(collection))
                                    }
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
                                    backStack.add(Destination.DetailCollection(collection))
                                },
                                navigateBack = {
                                    if (page == currentPage && backStack.size > 1) {
                                        backStack.removeAt(backStack.lastIndex)
                                    }
                                }
                            )
                        }
                        entry<Destination.Search> {

                        }
                        entry<Destination.Upload> {
                            
                        }
                        entry<Destination.Followed> {

                        }
                        entry<Destination.Profile> {
                            ProfileScreen()
                        }
                    }
                )
            }
        }

        CommentSheetComponent()

        NavigationBar(
            modifier = Modifier.align(Alignment.BottomCenter),
            isVisible = isNavBarVisible,
            selectedKey = selectedTopLevelRoute,
            onSelectKey = { key ->
                val route = key as? Destination ?: return@NavigationBar
                if (selectedTopLevelRoute != route || backStack.size > 1) {
                    backStack.clear()
                    backStack.add(route)
                }
            },
            items = navItems,
            hazeState = hazeState,
            style = navigationBarStyle
        )
    }
}