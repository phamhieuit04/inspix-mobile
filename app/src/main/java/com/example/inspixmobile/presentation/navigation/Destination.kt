package com.example.inspixmobile.presentation.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Star
import androidx.navigation3.runtime.NavKey
import com.example.inspixmobile.presentation.component.BottomNavItem
import kotlinx.serialization.Serializable

val TOP_LEVEL_DESTINATIONS: Map<NavKey, BottomNavItem> = mapOf(
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
}