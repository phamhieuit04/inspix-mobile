package com.example.inspixmobile.presentation.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
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
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.LocalNavAnimatedContentScope
import androidx.navigation3.ui.NavDisplay
import com.example.inspixmobile.core.event.Event
import com.example.inspixmobile.core.event.EventBus
import com.example.inspixmobile.core.util.ObserveAsEvents
import com.example.inspixmobile.domain.model.Session
import com.example.inspixmobile.domain.model.Setting
import com.example.inspixmobile.presentation.component.CommentSheetComponent
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource
import com.example.inspixmobile.presentation.component.NavigationBar
import com.example.inspixmobile.presentation.component.SignInRequiredDialog
import com.example.inspixmobile.presentation.component.TopShadowOverlay
import com.example.inspixmobile.presentation.screen.DetailArtistScreen
import com.example.inspixmobile.presentation.screen.DetailCollectionScreen
import com.example.inspixmobile.presentation.screen.DetailTopicScreen
import com.example.inspixmobile.presentation.screen.FollowingScreen
import com.example.inspixmobile.presentation.screen.HomeScreen
import com.example.inspixmobile.presentation.screen.ProfileScreen
import com.example.inspixmobile.presentation.screen.SearchResultScreen
import com.example.inspixmobile.presentation.screen.SearchScreen
import com.example.inspixmobile.presentation.screen.SettingScreen
import com.example.inspixmobile.presentation.screen.SignInScreen
import com.example.inspixmobile.presentation.state.rememberNavigationState
import com.example.inspixmobile.presentation.state.toEntries
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.launch

private const val IOS_DURATION = 500

@Composable
fun Graph(
    currentSetting: Setting,
    currentSession: Session
) {
    val scope = rememberCoroutineScope()

    val isLoggedIn = currentSession.isLoggedIn

    val topLevelRoutes by remember(currentSetting.navbarLayout, isLoggedIn) {
        derivedStateOf {
            topLevelRoutesFor(currentSetting.navbarLayout, isLoggedIn)
        }
    }
    val topLevelNavItems by remember(currentSetting.navbarLayout, isLoggedIn) {
        derivedStateOf {
            topLevelNavItemsFor(currentSetting.navbarLayout, isLoggedIn)
        }
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
    val showSignInDialog = remember { mutableStateOf(false) }

    val homeScrollSignal = scrollToTopSignals[Destination.Home] ?: 0
    val followingScrollSignal = scrollToTopSignals[Destination.Following] ?: 0
    val searchScrollSignal = scrollToTopSignals[Destination.Search] ?: 0
    val profileScrollSignal = scrollToTopSignals[Destination.Profile] ?: 0

    val dockedBarHeight = 60.dp
    val bottomContentPadding = dockedBarHeight + navInsetBottom + 36.dp

    val targetPage = remember(navigationState.topLevelRoute, topLevelRoutes) {
        navigationState.topLevelRoute.toTopLevelPageIndex(topLevelRoutes) ?: 0
    }

    val pagerState = rememberPagerState(
        initialPage = targetPage,
        pageCount = { topLevelRoutes.size }
    )

    val isUserScrollEnabled by remember(isDetailCollectionRoute) {
        derivedStateOf { !isDetailCollectionRoute }
    }

    val hazeState = remember { HazeState() }

    val iosPushTransform: AnimatedContentTransitionScope<*>.() -> ContentTransform = {
        slideInHorizontally(
            initialOffsetX = { it },
            animationSpec = tween(IOS_DURATION, easing = FastOutSlowInEasing)
        ) togetherWith
                slideOutHorizontally(
                    targetOffsetX = { -it / 4 },
                    animationSpec = tween(IOS_DURATION, easing = FastOutSlowInEasing)
                )
    }
    val iosPopTransform: AnimatedContentTransitionScope<*>.() -> ContentTransform = {
        slideInHorizontally(
            initialOffsetX = { -it / 4 },
            animationSpec = tween(IOS_DURATION, easing = LinearOutSlowInEasing)
        ) togetherWith
                slideOutHorizontally(
                    targetOffsetX = { it },
                    animationSpec = tween(IOS_DURATION, easing = LinearOutSlowInEasing)
                )
    }

    ObserveAsEvents(flow = EventBus.events) { event ->
        when (event) {
            Event.RequireSignIn -> {
                showSignInDialog.value = true
            }

            Event.SignOut -> {
                navigator.replaceAll(Destination.SignIn)
            }

            Event.SignIn -> {
                navigator.replaceAll(Destination.Profile)
            }
        }
    }

    LaunchedEffect(topLevelRoutes) {
        if (pagerState.currentPage != targetPage) {
            pagerState.scrollToPage(targetPage)
        }
    }

    LaunchedEffect(navigationState.topLevelRoute) {
        if (pagerState.currentPage != targetPage) {
            pagerState.animateScrollToPage(targetPage)
        }
    }

    LaunchedEffect(pagerState, topLevelRoutes) {
        snapshotFlow { pagerState.settledPage }
            .distinctUntilChanged()
            .drop(1)
            .collect { page ->
                val route = topLevelRouteForPage(page, topLevelRoutes)
                if (navigationState.topLevelRoute != route && !pagerState.isScrollInProgress) {
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
            beyondViewportPageCount = 4,
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
                            entry<Destination.Home>(
                                metadata = NavDisplay.transitionSpec(iosPushTransform) +
                                        NavDisplay.popTransitionSpec(iosPopTransform)
                            ) {
                                HomeScreen(
                                    sharedTransitionScope = this@SharedTransitionLayout,
                                    animatedVisibilityScope = LocalNavAnimatedContentScope.current,
                                    bottomContentPadding = bottomContentPadding,
                                    scrollToTopSignal = homeScrollSignal,
                                    layoutStyle = currentSetting.homeLayout,
                                    navigateToDetailCollection = { collection ->
                                        navigator.push(Destination.DetailCollection(collection))
                                    },
                                    navigateToDetailTopic = { topic ->
                                        navigator.push(Destination.DetailTopic(topic))
                                    },
                                    navigateToSearch = {
                                        navigator.switchTab(Destination.Search)
                                    },
                                    navigateToDetailArtist = { artist ->
                                        navigator.push(Destination.DetailArtist(artist))
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
                                    navigateToDetailArtist = { artist ->
                                        navigator.push(Destination.DetailArtist(artist))
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
                            entry<Destination.Following> {
                                FollowingScreen(
                                    sharedTransitionScope = this@SharedTransitionLayout,
                                    animatedVisibilityScope = LocalNavAnimatedContentScope.current,
                                    scrollToTopSignal = followingScrollSignal,
                                    bottomContentPadding = bottomContentPadding,
                                    navigateToDetailCollection = { collection ->
                                        navigator.push(Destination.DetailCollection(collection))
                                    },
                                    navigateToDetailArtist = { artist ->
                                        navigator.push(Destination.DetailArtist(artist))
                                    }
                                )
                            }
                            entry<Destination.Profile>(
                                metadata = NavDisplay.transitionSpec(iosPushTransform) +
                                        NavDisplay.popTransitionSpec(iosPopTransform)
                            ) {
                                val uuid = currentSession.userUuid ?: return@entry
                                ProfileScreen(
                                    uuid = uuid,
                                    sharedTransitionScope = this@SharedTransitionLayout,
                                    animatedVisibilityScope = LocalNavAnimatedContentScope.current,
                                    scrollToTopSignal = profileScrollSignal,
                                    bottomContentPadding = bottomContentPadding,
                                    navigateToSetting = {
                                        navigator.push(Destination.Setting)
                                    },
                                    navigateToDetail = { collection ->
                                        navigator.push(Destination.DetailCollection(collection))
                                    },
                                )
                            }
                            entry<Destination.SignIn> {
                                SignInScreen(
                                    sharedTransitionScope = this@SharedTransitionLayout,
                                    animatedVisibilityScope = LocalNavAnimatedContentScope.current
                                )
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
                            entry<Destination.Setting>(
                                metadata = NavDisplay.transitionSpec(iosPushTransform) +
                                        NavDisplay.popTransitionSpec(iosPopTransform)
                            ) {
                                SettingScreen(
                                    bottomContentPadding = bottomContentPadding,
                                    layoutStyle = currentSetting.homeLayout,
                                    navbarStyle = currentSetting.navbarLayout,
                                    onBackPressed = { navigator.goBack() }
                                )
                            }
                            entry<Destination.DetailArtist>(
                                metadata = NavDisplay.transitionSpec(iosPushTransform) +
                                        NavDisplay.popTransitionSpec(iosPopTransform)
                            ) { entry ->
                                val artist = entry.artist
                                DetailArtistScreen(
                                    artist = artist,
                                    sharedTransitionScope = this@SharedTransitionLayout,
                                    animatedVisibilityScope = LocalNavAnimatedContentScope.current,
                                    bottomContentPadding = bottomContentPadding,
                                    navigateBack = { navigator.goBack() },
                                    navigateToDetailCollection = { collection ->
                                        navigator.push(Destination.DetailCollection(collection))
                                    }
                                )
                            }
                        }
                    )
                )
            }
        }

        TopShadowOverlay()

        CommentSheetComponent(
            isLoggedIn = isLoggedIn
        )

        SignInRequiredDialog(
            visible = showSignInDialog.value,
            onDismiss = { showSignInDialog.value = false },
            navigateToSignIn = {
                showSignInDialog.value = false
                navigator.switchTab(Destination.SignIn)
            }
        )

        NavigationBar(
            modifier = Modifier.align(Alignment.BottomCenter),
            isVisible = isNavBarVisible,
            selectedKey = navigationState.topLevelRoute,
            onSelectKey = { route ->
                val isSameTab = route == navigationState.topLevelRoute
                val stack = navigationState.backStacks[route]

                if (isSameTab && stack != null) {
                    if (stack.size > 1) {
                        while (stack.size > 1) {
                            stack.removeLastOrNull()
                        }
                    } else {
                        scrollToTopSignals[route] =
                            (scrollToTopSignals[route] ?: 0) + 1
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
            style = currentSetting.navbarLayout
        )
    }
}