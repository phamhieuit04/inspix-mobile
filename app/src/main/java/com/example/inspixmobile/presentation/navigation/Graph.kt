package com.example.inspixmobile.presentation.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import com.example.inspixmobile.core.util.Navigator
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
        modifier = Modifier.fillMaxSize()
    ) {
        NavDisplay(
            onBack = navigator::goBack,
            entries = navigationState.toEntries(
                entryProvider {
                    entry<Destination.Home> {

                    }
                }
            )
        )
    }
}