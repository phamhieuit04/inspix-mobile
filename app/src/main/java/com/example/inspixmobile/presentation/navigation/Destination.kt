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
import com.example.inspixmobile.presentation.component.BottomNavItem
import com.example.inspixmobile.presentation.component.NavigationBarStyle
import kotlinx.serialization.Serializable

val DOCKED_TOP_LEVEL_ROUTES: List<NavKey> = listOf(
    Destination.Home,
    Destination.Search,
    Destination.Upload,
    Destination.Followed,
    Destination.Profile
)

val FLOATING_TOP_LEVEL_PILL_ROUTES: List<NavKey> = listOf(
    Destination.Home,
    Destination.Followed,
    Destination.Upload,
    Destination.Profile
)

val FLOATING_TOP_LEVEL_SEARCH_ROUTE: NavKey = Destination.Search

val FLOATING_TOP_LEVEL_ROUTES: List<NavKey> =
    FLOATING_TOP_LEVEL_PILL_ROUTES + FLOATING_TOP_LEVEL_SEARCH_ROUTE


val DOCKED_TOP_LEVEL_NAV_ITEMS: Map<NavKey, BottomNavItem> = linkedMapOf(
    Destination.Home to BottomNavItem(
        "Khám phá", Icons.Outlined.Image, Icons.Default.Image
    ),
    Destination.Search to BottomNavItem(
        "Tìm kiếm", Icons.Outlined.Search, Icons.Default.Search
    ),
    Destination.Upload to BottomNavItem(
        "Đăng tải", Icons.Outlined.Add, Icons.Default.AddCircle
    ),
    Destination.Followed to BottomNavItem(
        "Theo dõi", Icons.Outlined.Group, Icons.Default.Group
    ),
    Destination.Profile to BottomNavItem(
        "Hồ sơ", Icons.Outlined.AccountCircle, Icons.Default.AccountCircle
    )
)

val FLOATING_TOP_LEVEL_NAV_ITEMS: Map<NavKey, BottomNavItem> = linkedMapOf(
    Destination.Home to BottomNavItem(
        "Khám phá", Icons.Outlined.Image, Icons.Default.Image
    ),
    Destination.Upload to BottomNavItem(
        "Đăng tải", Icons.Outlined.Add, Icons.Default.AddCircle
    ),
    Destination.Followed to BottomNavItem(
        "Theo dõi", Icons.Outlined.Group, Icons.Default.Group
    ),
    Destination.Profile to BottomNavItem(
        "Hồ sơ", Icons.Outlined.AccountCircle, Icons.Default.AccountCircle
    ),
    Destination.Search to BottomNavItem(
        "Tìm kiếm", Icons.Outlined.Search, Icons.Default.Search
    ),
)

val ALL_TOP_LEVEL_ROUTES: Set<NavKey> =
    (DOCKED_TOP_LEVEL_ROUTES + FLOATING_TOP_LEVEL_ROUTES).toSet()


@Serializable
sealed class Destination : NavKey {
    @Serializable
    object Home : Destination()

    @Serializable
    object Search : Destination()

    @Serializable
    object Upload : Destination()

    @Serializable
    object Followed : Destination()

    @Serializable
    object Profile : Destination()

    @Serializable
    data class DetailCollection(val collection: Collection) : Destination()
}