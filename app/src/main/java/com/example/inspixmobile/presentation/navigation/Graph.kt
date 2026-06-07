package com.example.inspixmobile.presentation.navigation

import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.LocalNavAnimatedContentScope
import androidx.navigation3.ui.NavDisplay
import com.example.inspixmobile.domain.model.Session
import com.example.inspixmobile.domain.model.Setting
import com.example.inspixmobile.presentation.component.CommentSheetComponent
import dev.chrisbanes.haze.HazeState
import com.example.inspixmobile.presentation.component.NavigationBar
import com.example.inspixmobile.presentation.component.TopShadowOverlay
import com.example.inspixmobile.presentation.screen.DetailCollectionScreen
import com.example.inspixmobile.presentation.screen.DetailTopicScreen
import com.example.inspixmobile.presentation.screen.HomeScreen
import com.example.inspixmobile.presentation.screen.ProfileScreen
import com.example.inspixmobile.presentation.screen.SearchResultScreen
import com.example.inspixmobile.presentation.screen.SearchScreen
import com.example.inspixmobile.presentation.screen.SettingScreen
import com.example.inspixmobile.presentation.screen.SignInScreen
import com.example.inspixmobile.presentation.state.rememberNavigationState
import com.example.inspixmobile.presentation.state.toEntries
import dev.chrisbanes.haze.hazeSource

@Composable
fun Graph(
    currentSetting: Setting,
    currentSession: Session
) {
    val scope = rememberCoroutineScope()

    val topLevelNavItems by remember(currentSetting.navbarLayout) {
        derivedStateOf {
            topLevelNavItemsFor(
                currentSetting.navbarLayout
            )
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

    val homeScrollSignal = scrollToTopSignals[Destination.Home] ?: 0
    val searchScrollSignal = scrollToTopSignals[Destination.Search] ?: 0

    val dockedBarHeight = 60.dp
    val bottomContentPadding = dockedBarHeight + navInsetBottom + 36.dp

    val hazeState = remember { HazeState() }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        SharedTransitionLayout {
            NavDisplay(
                onBack = navigator::goBack,
                modifier = Modifier
                    .fillMaxSize()
                    .hazeSource(hazeState),
                entries = navigationState.toEntries(
                    entryProvider {
                        entry<Destination.Home> {
                            HomeScreen(
                                sharedTransitionScope = this@SharedTransitionLayout,
                                animatedVisibilityScope = LocalNavAnimatedContentScope.current,
                                bottomContentPadding = bottomContentPadding,
                                scrollToTopSignal = homeScrollSignal,
                                layoutStyle = currentSetting.homeLayout,
                                navigateToDetailCollection = { collection ->
                                    navigator.navigate(Destination.DetailCollection(collection))
                                },
                                navigateToDetailTopic = { topic ->
                                    navigator.navigate(Destination.DetailTopic(topic))
                                },
                                navigateToSearch = {
                                    navigator.navigate(Destination.Search)
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
                                    navigator.navigate(Destination.DetailCollection(collection))
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
                                    navigator.navigate(Destination.DetailTopic(topic))
                                },
                                navigateToSearchResult = { query ->
                                    navigator.navigate(Destination.SearchResult(query))
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
                                    navigator.navigate(Destination.DetailCollection(collection))
                                },
                                navigateBack = { navigator.goBack() }
                            )
                        }
                        entry<Destination.Upload> {

                        }
                        entry<Destination.Followed> {

                        }
                        entry<Destination.Profile> {
                            if (currentSession.isLoggedIn) {
                                ProfileScreen(
                                    uuid = currentSession.userUuid!!,
                                    navigateToSetting = {
                                        navigator.navigate(Destination.Setting)
                                    }
                                )
                            } else {
                                SignInScreen()
                            }
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
                                    navigator.navigate(Destination.DetailCollection(collection))
                                },
                            )
                        }
                        entry<Destination.Setting> {
                            SettingScreen(
                                bottomContentPadding = bottomContentPadding,
                                layoutStyle = currentSetting.homeLayout,
                                navbarStyle = currentSetting.navbarLayout,
                                onBackPressed = { navigator.goBack() },
                                onLogout = { }
                            )
                        }
                    })
            )
        }

        TopShadowOverlay()

        CommentSheetComponent()

        NavigationBar(
            modifier = Modifier.align(Alignment.BottomCenter),
            isVisible = isNavBarVisible,
            selectedKey = navigationState.topLevelRoute,
            onSelectKey = { navigator.navigate(it) },
            items = topLevelNavItems,
            hazeState = hazeState,
            style = currentSetting.navbarLayout
        )
    }
}