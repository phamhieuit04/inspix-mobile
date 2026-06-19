package com.example.inspixmobile.presentation.navigation

import androidx.activity.compose.LocalActivity
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
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
import androidx.compose.runtime.DisposableEffect
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
import androidx.core.view.WindowInsetsControllerCompat
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.LocalNavAnimatedContentScope
import androidx.navigation3.ui.NavDisplay
import androidx.window.core.layout.WindowSizeClass
import com.example.inspixmobile.core.event.Event
import com.example.inspixmobile.core.event.EventBus
import com.example.inspixmobile.core.util.ObserveAsEvents
import com.example.inspixmobile.domain.model.Session
import com.example.inspixmobile.domain.model.Setting
import com.example.inspixmobile.presentation.component.CommentSheetComponent
import com.example.inspixmobile.presentation.component.InteractionErrorDialog
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
import com.example.inspixmobile.presentation.screen.ImagesViewerScreen
import com.example.inspixmobile.presentation.screen.ProfileScreen
import com.example.inspixmobile.presentation.screen.SearchResultScreen
import com.example.inspixmobile.presentation.screen.SearchScreen
import com.example.inspixmobile.presentation.screen.SettingScreen
import com.example.inspixmobile.presentation.screen.SignInScreen
import com.example.inspixmobile.presentation.screen.UploadCameraScreen
import com.example.inspixmobile.presentation.screen.UploadSubmitScreen
import com.example.inspixmobile.presentation.state.rememberNavigationState
import com.example.inspixmobile.presentation.state.toEntries
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.launch

private const val IOS_DURATION = 500

@Composable
fun Graph(
    currentSetting: Setting,
    currentSession: Session,
    isTablet: Boolean = false
) {
    val scope = rememberCoroutineScope()
    val activity = LocalActivity.current

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

    val isNavBarVisible by remember(currentRoute) {
        derivedStateOf {
            currentRoute !is HideNavBarDestination
        }
    }

    val isUserScrollEnabled by remember(currentRoute) {
        derivedStateOf {
            currentRoute !is DisableScrollDestination
        }
    }

    val isImmersive by remember(currentRoute) {
        derivedStateOf {
            currentRoute is ImmersiveDestination
        }
    }

    val showSignInDialog = remember { mutableStateOf(false) }
    val showInteractionErrorDialog = remember { mutableStateOf(false) }

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

    val messageDialog = remember { mutableStateOf<String?>(null) }

    ObserveAsEvents(flow = EventBus.events) { event ->
        when (event) {
            Event.RequireSignIn -> {
                showSignInDialog.value = true
            }

            Event.SignOutSuccess -> {
                navigator.replaceAll(Destination.SignIn)
            }

            Event.SignInSuccess -> {
                navigator.switchCurrentTabTo(Destination.Profile)
            }

            Event.NetworkError -> {
                showInteractionErrorDialog.value = true
            }

            is Event.ShowMessage -> {
                messageDialog.value = event.message
            }

            Event.UploadSuccess -> {
                navigator.popToRoot()
                navigator.switchTab(Destination.Profile)
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

    DisposableEffect(currentRoute) {
        val window = activity?.window ?: return@DisposableEffect onDispose { }
        val controller = WindowInsetsControllerCompat(window, window.decorView)

        if (currentRoute is ImmersiveDestination) {
            controller.isAppearanceLightStatusBars = false
            controller.isAppearanceLightNavigationBars = false
        } else {
            controller.isAppearanceLightStatusBars = true
            controller.isAppearanceLightNavigationBars = true
        }

        onDispose { }
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
                                    isTablet = isTablet,
                                    sharedTransitionScope = this@SharedTransitionLayout,
                                    animatedVisibilityScope = LocalNavAnimatedContentScope.current,
                                    bottomContentPadding = bottomContentPadding,
                                    scrollToTopSignal = homeScrollSignal,
                                    layoutStyle = currentSetting.homeLayout,
                                    navigateToDetailCollection = { collection, page ->
                                        navigator.push(
                                            Destination.DetailCollection(
                                                collection,
                                                page
                                            )
                                        )
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
                                val page = entry.page

                                DetailCollectionScreen(
                                    isTablet = isTablet,
                                    currentSession = currentSession,
                                    collection = collection,
                                    initialPage = page,
                                    sharedTransitionScope = this@SharedTransitionLayout,
                                    animatedVisibilityScope = LocalNavAnimatedContentScope.current,
                                    bottomContentPadding = bottomContentPadding,
                                    navigateToDetailCollection = { collection, page ->
                                        navigator.push(
                                            Destination.DetailCollection(
                                                collection,
                                                page
                                            )
                                        )
                                    },
                                    navigateToDetailArtist = { artist ->
                                        navigator.push(Destination.DetailArtist(artist))
                                    },
                                    navigateToImagesViewer = { images, initialPage ->
                                        navigator.push(
                                            Destination.ImagesViewer(
                                                images,
                                                initialPage
                                            )
                                        )
                                    },
                                    navigateToProfile = {
                                        navigator.switchTab(Destination.Profile)
                                    },
                                    navigateBack = { navigator.goBack() }
                                )
                            }
                            entry<Destination.Search> {
                                SearchScreen(
                                    isTablet = isTablet,
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
                                    isTablet = isTablet,
                                    query = query,
                                    sharedTransitionScope = this@SharedTransitionLayout,
                                    animatedVisibilityScope = LocalNavAnimatedContentScope.current,
                                    bottomContentPadding = bottomContentPadding,
                                    navigateToDetailCollection = { collection ->
                                        navigator.push(
                                            Destination.DetailCollection(
                                                collection,
                                                null
                                            )
                                        )
                                    },
                                    navigateBack = { navigator.goBack() }
                                )
                            }
                            entry<Destination.Following> {
                                FollowingScreen(
                                    sharedTransitionScope = this@SharedTransitionLayout,
                                    animatedVisibilityScope = LocalNavAnimatedContentScope.current,
                                    scrollToTopSignal = followingScrollSignal,
                                    bottomContentPadding = bottomContentPadding,
                                    navigateToDetailCollection = { collection, page ->
                                        navigator.push(
                                            Destination.DetailCollection(
                                                collection,
                                                page
                                            )
                                        )
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
                                    isTablet = isTablet,
                                    uuid = uuid,
                                    sharedTransitionScope = this@SharedTransitionLayout,
                                    animatedVisibilityScope = LocalNavAnimatedContentScope.current,
                                    scrollToTopSignal = profileScrollSignal,
                                    bottomContentPadding = bottomContentPadding,
                                    navigateToSetting = {
                                        navigator.push(Destination.Setting)
                                    },
                                    navigateToDetail = { collection ->
                                        navigator.push(
                                            Destination.DetailCollection(
                                                collection,
                                                null
                                            )
                                        )
                                    },
                                )
                            }
                            entry<Destination.SignIn> {
                                SignInScreen(
                                    isTablet = isTablet,
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
                                        navigator.push(
                                            Destination.DetailCollection(
                                                collection,
                                                null
                                            )
                                        )
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
                                        navigator.push(
                                            Destination.DetailCollection(
                                                collection,
                                                null
                                            )
                                        )
                                    }
                                )
                            }
                            entry<Destination.ImagesViewer> { entry ->
                                val images = entry.images
                                val initialPage = entry.initialPage

                                ImagesViewerScreen(
                                    images = images,
                                    initialPage = initialPage,
                                    sharedTransitionScope = this@SharedTransitionLayout,
                                    animatedVisibilityScope = LocalNavAnimatedContentScope.current,
                                    navigateBack = { navigator.goBack() }
                                )
                            }
                            entry<Destination.UploadCamera>(
                                metadata = NavDisplay.transitionSpec(iosPushTransform) +
                                        NavDisplay.popTransitionSpec(iosPopTransform)
                            ) {
                                val isCurrentScreen = currentRoute is Destination.UploadCamera

                                UploadCameraScreen(
                                    isCurrentScreen = isCurrentScreen,
                                    bottomContentPadding = bottomContentPadding,
                                    navigateToUploadSubmit = {
                                        navigator.push(Destination.UploadSubmit)
                                    }
                                )
                            }
                            entry<Destination.UploadSubmit>(
                                metadata = NavDisplay.transitionSpec(iosPushTransform) +
                                        NavDisplay.popTransitionSpec(iosPopTransform)
                            ) {
                                UploadSubmitScreen(
                                    sharedTransitionScope = this@SharedTransitionLayout,
                                    bottomContentPadding = bottomContentPadding,
                                    navigateToImagesViewer = { images, initialPage ->
                                        navigator.push(
                                            Destination.ImagesViewer(
                                                images,
                                                initialPage
                                            )
                                        )
                                    },
                                    navigateBack = { navigator.goBack() }
                                )
                            }
                        }
                    )
                )
            }
        }

        TopShadowOverlay(
            visible = !isImmersive,
            height = if (isTablet) 48.dp else 60.dp
        )

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

        InteractionErrorDialog(
            visible = showInteractionErrorDialog.value,
            onDismiss = { showInteractionErrorDialog.value = false },
        )

        messageDialog.value?.let { message ->
            InteractionErrorDialog(
                message = message,
                onDismiss = { messageDialog.value = null }
            )
        }

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