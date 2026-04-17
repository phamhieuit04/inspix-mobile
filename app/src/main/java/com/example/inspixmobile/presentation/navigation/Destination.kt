package com.example.inspixmobile.presentation.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Search
import androidx.navigation3.runtime.NavKey
import com.example.inspixmobile.presentation.component.BottomNavItem
import kotlinx.serialization.Serializable

val TOP_LEVEL_DESTINATIONS: Map<NavKey, BottomNavItem> = mapOf(
    Destination.Home to BottomNavItem(
        "Trang chủ", Icons.Outlined.Home, Icons.Default.Home
    ),
    Destination.Search to BottomNavItem(
        "Khám phá", Icons.Outlined.Search, Icons.Default.Search
    ),
    Destination.Upload to BottomNavItem(
        "Đăng tải", Icons.Outlined.Add, Icons.Default.AddCircle
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
    object Profile : Destination()
}