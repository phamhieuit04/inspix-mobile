package com.example.inspixmobile.presentation.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource
import com.example.inspixmobile.presentation.component.NavigationBar
import com.example.inspixmobile.presentation.component.NavigationBarStyle
import com.example.inspixmobile.presentation.screen.HomeScreen
import com.example.inspixmobile.presentation.state.rememberNavigationState
import com.example.inspixmobile.presentation.state.toEntries

@Composable
fun Graph() {
    val navigationState = rememberNavigationState(
        startRoute = Destination.Home,
        topLevelRoutes = TOP_LEVEL_DESTINATIONS.keys
    )

    val navigator = remember { Navigator(navigationState) }
    val hazeState = remember { HazeState() }
    val navInsetBottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    val dockedBarHeight = 60.dp
    val bottomContentPadding = dockedBarHeight + navInsetBottom + 36.dp

    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        NavDisplay(
            onBack = navigator::goBack,
            modifier = Modifier
                .fillMaxSize()
                .hazeSource(state = hazeState),
            entries = navigationState.toEntries(
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
            )
        )

        NavigationBar(
            modifier = Modifier.align(Alignment.BottomCenter),
            selectedKey = navigationState.topLevelRoute,
            onSelectKey = { navigator.navigate(it) },
            items = TOP_LEVEL_DESTINATIONS,
            hazeState = hazeState,
            style = NavigationBarStyle.Float
        )
    }
}