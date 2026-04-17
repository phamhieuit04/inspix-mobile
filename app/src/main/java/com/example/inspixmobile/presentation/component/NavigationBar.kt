package com.example.inspixmobile.presentation.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation3.runtime.NavKey
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.materials.ExperimentalHazeMaterialsApi
import dev.chrisbanes.haze.materials.HazeMaterials

enum class NavigationBarStyle { Float, Docked }

data class BottomNavItem(
    val label: String,
    val icon: ImageVector,
    val selectedIcon: ImageVector
)

@OptIn(ExperimentalHazeMaterialsApi::class)
@Composable
fun NavigationBar(
    modifier: Modifier = Modifier,
    isVisible: Boolean = true,
    selectedKey: NavKey,
    onSelectKey: (NavKey) -> Unit,
    items: Map<NavKey, BottomNavItem>,
    hazeState: HazeState,
    style: NavigationBarStyle = NavigationBarStyle.Float,
) {
    if (!isVisible) return

    val navBarPadding = WindowInsets.navigationBars.asPaddingValues()

    when (style) {
        NavigationBarStyle.Float -> FloatingNavigationBar(
            modifier = modifier.padding(bottom = navBarPadding.calculateBottomPadding() + 12.dp),
            selectedKey = selectedKey,
            onSelectKey = onSelectKey,
            items = items,
            hazeState = hazeState
        )

        NavigationBarStyle.Docked -> DockedNavigationBar(
            modifier = modifier,
            selectedKey = selectedKey,
            onSelectKey = onSelectKey,
            items = items,
            hazeState = hazeState,
            bottomPadding = navBarPadding.calculateBottomPadding()
        )
    }
}

@OptIn(ExperimentalHazeMaterialsApi::class)
@Composable
private fun FloatingNavigationBar(
    modifier: Modifier = Modifier,
    selectedKey: NavKey,
    onSelectKey: (NavKey) -> Unit,
    items: Map<NavKey, BottomNavItem>,
    hazeState: HazeState,
) {
    val pillOrder = listOf("Home", "Upload", "Profile")
    val pillItems = pillOrder.mapNotNull { label -> items.entries.find { it.value.label == label } }
    val searchEntry = items.entries.find { it.value.label == "Search" }

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(50))
                .hazeEffect(state = hazeState, style = HazeMaterials.ultraThin())
                .background(Color.White.copy(alpha = 0.15f))
                .padding(horizontal = 6.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            pillItems.forEach { (key, item) ->
                val isSelected = selectedKey == key
                FloatingNavItem(
                    icon = if (isSelected) item.selectedIcon else item.icon,
                    label = item.label,
                    isSelected = isSelected,
                    onClick = { onSelectKey(key) }
                )
            }
        }

        if (searchEntry != null) {
            val (searchKey, searchItem) = searchEntry
            val isSelected = selectedKey == searchKey
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .hazeEffect(state = hazeState, style = HazeMaterials.ultraThin())
                    .background(
                        if (isSelected) Color(0xFF7B4FBF).copy(alpha = 0.2f)
                        else Color.White.copy(alpha = 0.15f)
                    )
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { onSelectKey(searchKey) },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isSelected) searchItem.selectedIcon else searchItem.icon,
                    contentDescription = searchItem.label,
                    tint = if (isSelected) Color(0xFF7B4FBF) else Color(0xFF5C5C7A),
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}

@Composable
private fun FloatingNavItem(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }

    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .then(
                if (isSelected) Modifier.background(Color(0xFF7B4FBF).copy(alpha = 0.15f))
                else Modifier
            )
            .clickable(interactionSource = interactionSource, indication = null) { onClick() }
            .padding(horizontal = if (isSelected) 14.dp else 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (isSelected) Color(0xFF7B4FBF) else Color(0xFF5C5C7A),
            modifier = Modifier.size(22.dp)
        )
        if (isSelected) {
            Text(
                text = label,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF7B4FBF)
            )
        }
    }
}

@OptIn(ExperimentalHazeMaterialsApi::class)
@Composable
private fun DockedNavigationBar(
    modifier: Modifier = Modifier,
    selectedKey: NavKey,
    onSelectKey: (NavKey) -> Unit,
    items: Map<NavKey, BottomNavItem>,
    hazeState: HazeState,
    bottomPadding: Dp,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .hazeEffect(state = hazeState, style = HazeMaterials.thin())
            .background(Color.White.copy(alpha = 0.1f))
            .padding(horizontal = 8.dp)
            .padding(top = 4.dp, bottom = 4.dp + bottomPadding),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
    ) {
        items.forEach { (key, item) ->
            val isSelected = selectedKey == key
            DockedNavItem(
                icon = if (isSelected) item.selectedIcon else item.icon,
                label = item.label,
                isSelected = isSelected,
                onClick = { onSelectKey(key) }
            )
        }
    }
}

@Composable
private fun DockedNavItem(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }

    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(interactionSource = interactionSource, indication = null) { onClick() }
            .padding(horizontal = 12.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(50))
                .then(
                    if (isSelected) Modifier.background(Color(0xFF7B4FBF).copy(alpha = 0.12f))
                    else Modifier
                )
                .padding(horizontal = 16.dp, vertical = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isSelected) Color(0xFF7B4FBF) else Color(0xFF888899),
                modifier = Modifier.size(24.dp)
            )
        }
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            color = if (isSelected) Color(0xFF7B4FBF) else Color(0xFF888899)
        )
    }
}