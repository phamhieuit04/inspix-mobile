package com.example.inspixmobile.presentation.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import com.example.inspixmobile.presentation.component.NavigationBar
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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
    ) {
        NavDisplay(
            onBack = navigator::goBack,
            entries = navigationState.toEntries(
                entryProvider {
                    entry<Destination.Home> {
                        HomeScreen()
                    }
                    entry<Destination.Search> {

                    }
                    entry<Destination.Upload> {

                    }
                    entry<Destination.Profile> {

                    }
                }
            )
        )

        NavigationBar(
            modifier = Modifier.align(Alignment.BottomCenter),
            selectedKey = navigationState.topLevelRoute,
            onSelectKey = {
                navigator.navigate(it)
            }
        )
    }
}