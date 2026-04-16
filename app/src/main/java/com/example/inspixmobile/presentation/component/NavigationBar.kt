package com.example.inspixmobile.presentation.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation3.runtime.NavKey

data class BottomNavItem(
    val label: String,
    val icon: ImageVector,
    val selectedIcon: ImageVector
)

@Composable
fun NavigationBar(
    modifier: Modifier = Modifier,
    isVisible: Boolean = true,
    selectedKey: NavKey,
    onSelectKey: (NavKey) -> Unit,
) {
    if (!isVisible) return
}