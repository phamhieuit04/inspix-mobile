package com.example.inspixmobile.presentation.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Search
import androidx.navigation3.runtime.NavKey
import com.example.inspixmobile.domain.model.Collection
import com.example.inspixmobile.domain.model.Topic
import com.example.inspixmobile.presentation.component.BottomNavItem
import com.example.inspixmobile.presentation.component.NavigationBarStyle
import kotlinx.serialization.Serializable

val DOCKED_TOP_LEVEL_ROUTES_LOGGED_IN: List<NavKey> = listOf(
    Destination.Home,
    Destination.Search,
    Destination.Upload,
    Destination.Following,
    Destination.Profile
)

val DOCKED_TOP_LEVEL_ROUTES_LOGGED_OUT: List<NavKey> = listOf(
    Destination.Home,
    Destination.Search,
    Destination.Upload,
    Destination.Following,
    Destination.SignIn
)

val FLOATING_TOP_LEVEL_PILL_ROUTES_LOGGED_IN: List<NavKey> = listOf(
    Destination.Home,
    Destination.Following,
    Destination.Upload,
    Destination.Profile
)

val FLOATING_TOP_LEVEL_PILL_ROUTES_LOGGED_OUT: List<NavKey> = listOf(
    Destination.Home,
    Destination.Following,
    Destination.Upload,
    Destination.SignIn
)

val FLOATING_TOP_LEVEL_SEARCH_ROUTE: NavKey = Destination.Search

val FLOATING_TOP_LEVEL_ROUTES_LOGGED_IN: List<NavKey> =
    FLOATING_TOP_LEVEL_PILL_ROUTES_LOGGED_IN + FLOATING_TOP_LEVEL_SEARCH_ROUTE

val FLOATING_TOP_LEVEL_ROUTES_LOGGED_OUT: List<NavKey> =
    FLOATING_TOP_LEVEL_PILL_ROUTES_LOGGED_OUT + FLOATING_TOP_LEVEL_SEARCH_ROUTE

val DOCKED_TOP_LEVEL_NAV_ITEMS_LOGGED_IN: Map<NavKey, BottomNavItem> = linkedMapOf(
    Destination.Home to BottomNavItem(
        "Khám phá", Icons.Outlined.Image, Icons.Default.Image
    ),
    Destination.Search to BottomNavItem(
        "Tìm kiếm", Icons.Outlined.Search, Icons.Default.Search
    ),
    Destination.Upload to BottomNavItem(
        "Đăng tải", Icons.Outlined.Add, Icons.Default.AddCircle
    ),
    Destination.Following to BottomNavItem(
        "Theo dõi", Icons.Outlined.Group, Icons.Default.Group
    ),
    Destination.Profile to BottomNavItem(
        "Hồ sơ", Icons.Outlined.AccountCircle, Icons.Default.AccountCircle
    )
)

val DOCKED_TOP_LEVEL_NAV_ITEMS_LOGGED_OUT: Map<NavKey, BottomNavItem> = linkedMapOf(
    Destination.Home to BottomNavItem(
        "Khám phá", Icons.Outlined.Image, Icons.Default.Image
    ),
    Destination.Search to BottomNavItem(
        "Tìm kiếm", Icons.Outlined.Search, Icons.Default.Search
    ),
    Destination.Upload to BottomNavItem(
        "Đăng tải", Icons.Outlined.Add, Icons.Default.AddCircle
    ),
    Destination.Following to BottomNavItem(
        "Theo dõi", Icons.Outlined.Group, Icons.Default.Group
    ),
    Destination.SignIn to BottomNavItem(
        "Hồ sơ", Icons.Outlined.AccountCircle, Icons.Default.AccountCircle
    )
)

val FLOATING_TOP_LEVEL_NAV_ITEMS_LOGGED_IN: Map<NavKey, BottomNavItem> = linkedMapOf(
    Destination.Home to requireNotNull(DOCKED_TOP_LEVEL_NAV_ITEMS_LOGGED_IN[Destination.Home]),
    Destination.Following to requireNotNull(DOCKED_TOP_LEVEL_NAV_ITEMS_LOGGED_IN[Destination.Following]),
    Destination.Upload to requireNotNull(DOCKED_TOP_LEVEL_NAV_ITEMS_LOGGED_IN[Destination.Upload]),
    Destination.Profile to requireNotNull(DOCKED_TOP_LEVEL_NAV_ITEMS_LOGGED_IN[Destination.Profile]),
    Destination.Search to requireNotNull(DOCKED_TOP_LEVEL_NAV_ITEMS_LOGGED_IN[Destination.Search]),
)

val FLOATING_TOP_LEVEL_NAV_ITEMS_LOGGED_OUT: Map<NavKey, BottomNavItem> = linkedMapOf(
    Destination.Home to requireNotNull(DOCKED_TOP_LEVEL_NAV_ITEMS_LOGGED_OUT[Destination.Home]),
    Destination.Following to requireNotNull(DOCKED_TOP_LEVEL_NAV_ITEMS_LOGGED_OUT[Destination.Following]),
    Destination.Upload to requireNotNull(DOCKED_TOP_LEVEL_NAV_ITEMS_LOGGED_OUT[Destination.Upload]),
    Destination.SignIn to requireNotNull(DOCKED_TOP_LEVEL_NAV_ITEMS_LOGGED_OUT[Destination.SignIn]),
    Destination.Search to requireNotNull(DOCKED_TOP_LEVEL_NAV_ITEMS_LOGGED_OUT[Destination.Search]),
)

val ALL_TOP_LEVEL_ROUTES: Set<NavKey> =
    (DOCKED_TOP_LEVEL_ROUTES_LOGGED_IN + DOCKED_TOP_LEVEL_ROUTES_LOGGED_OUT +
            FLOATING_TOP_LEVEL_ROUTES_LOGGED_IN + FLOATING_TOP_LEVEL_ROUTES_LOGGED_OUT).toSet()

fun topLevelRoutesFor(style: NavigationBarStyle, isLoggedIn: Boolean): List<NavKey> = when (style) {
    NavigationBarStyle.Docked -> if (isLoggedIn) DOCKED_TOP_LEVEL_ROUTES_LOGGED_IN else DOCKED_TOP_LEVEL_ROUTES_LOGGED_OUT
    NavigationBarStyle.Floating -> if (isLoggedIn) FLOATING_TOP_LEVEL_ROUTES_LOGGED_IN else FLOATING_TOP_LEVEL_ROUTES_LOGGED_OUT
}

fun topLevelNavItemsFor(
    style: NavigationBarStyle,
    isLoggedIn: Boolean
): Map<NavKey, BottomNavItem> = when (style) {
    NavigationBarStyle.Docked -> if (isLoggedIn) DOCKED_TOP_LEVEL_NAV_ITEMS_LOGGED_IN else DOCKED_TOP_LEVEL_NAV_ITEMS_LOGGED_OUT
    NavigationBarStyle.Floating -> if (isLoggedIn) FLOATING_TOP_LEVEL_NAV_ITEMS_LOGGED_IN else FLOATING_TOP_LEVEL_NAV_ITEMS_LOGGED_OUT
}

fun NavKey.toTopLevelPageIndex(routes: List<NavKey>): Int? {
    val index = routes.indexOf(this)
    return if (index >= 0) index else null
}

fun topLevelRouteForPage(index: Int, routes: List<NavKey>): NavKey =
    routes.getOrElse(index) { Destination.Home }

@Serializable
sealed class Destination : NavKey {
    @Serializable
    object Home : Destination()

    @Serializable
    object Search : Destination()

    @Serializable
    object Upload : Destination()

    @Serializable
    object Following : Destination()

    @Serializable
    object Profile : Destination()

    @Serializable
    object SignIn : Destination()

    @Serializable
    data class DetailCollection(val collection: Collection) : Destination()

    @Serializable
    data class DetailTopic(val topic: Topic) : Destination()

    @Serializable
    data class SearchResult(val query: String) : Destination()

    @Serializable
    object Setting : Destination()
}