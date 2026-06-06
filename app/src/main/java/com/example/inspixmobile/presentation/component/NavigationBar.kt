package com.example.inspixmobile.presentation.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDp
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation3.runtime.NavKey
import com.example.inspixmobile.core.extension.noRippleClickable
import com.example.inspixmobile.presentation.navigation.FLOATING_TOP_LEVEL_PILL_ROUTES
import com.example.inspixmobile.presentation.navigation.FLOATING_TOP_LEVEL_SEARCH_ROUTE
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.materials.CupertinoMaterials
import dev.chrisbanes.haze.materials.ExperimentalHazeMaterialsApi
import dev.chrisbanes.haze.materials.HazeMaterials

enum class NavigationBarStyle { Floating, Docked }

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
    style: NavigationBarStyle = NavigationBarStyle.Floating,
) {
    val navBarPadding = WindowInsets.navigationBars.asPaddingValues()

    val transition = updateTransition(
        targetState = isVisible,
        label = "nav_bar"
    )

    val translationY by transition.animateDp(
        label = "translationY",
        transitionSpec = {
            if (targetState) {
                tween(
                    durationMillis = 480,
                    delayMillis = 800,
                    easing = FastOutSlowInEasing
                )
            } else {
                tween(
                    durationMillis = 480,
                    delayMillis = 800,
                    easing = FastOutSlowInEasing
                )
            }
        }
    ) { visible ->
        if (visible) 0.dp else 120.dp
    }

    val alpha by transition.animateFloat(
        label = "alpha",
        transitionSpec = {
            if (targetState) {
                tween(
                    durationMillis = 400,
                    delayMillis = 800,
                    easing = FastOutSlowInEasing
                )
            } else {
                tween(
                    durationMillis = 280,
                    delayMillis = 800,
                    easing = FastOutSlowInEasing
                )
            }
        }
    ) { visible ->
        if (visible) 1f else 0f
    }

    Box(
        modifier = modifier
            .offset(y = translationY)
            .alpha(alpha)
    ) {
        when (style) {
            NavigationBarStyle.Floating -> {
                FloatingNavigationBar(
                    modifier = Modifier.padding(
                        bottom = navBarPadding.calculateBottomPadding() + 12.dp
                    ),
                    selectedKey = selectedKey,
                    onSelectKey = onSelectKey,
                    items = items,
                    hazeState = hazeState
                )
            }

            NavigationBarStyle.Docked -> {
                DockedNavigationBar(
                    selectedKey = selectedKey,
                    onSelectKey = onSelectKey,
                    items = items,
                    hazeState = hazeState,
                    bottomPadding = navBarPadding.calculateBottomPadding()
                )
            }
        }
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
    val pillItems =
        FLOATING_TOP_LEVEL_PILL_ROUTES.mapNotNull { key ->
            items[key]?.let { key to it }
        }

    val searchEntry = items[FLOATING_TOP_LEVEL_SEARCH_ROUTE]
        ?.let { FLOATING_TOP_LEVEL_SEARCH_ROUTE to it }

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Row(
            modifier = Modifier
                .height(60.dp)
                .clip(RoundedCornerShape(50))
                .hazeEffect(
                    state = hazeState,
                    style = CupertinoMaterials.thin()
                )
                .background(Color.White.copy(alpha = 0.15f))
                .padding(horizontal = 6.dp),
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
                    .height(60.dp)
                    .aspectRatio(1f)
                    .clip(CircleShape)
                    .hazeEffect(
                        state = hazeState,
                        style = CupertinoMaterials.thin()
                    )
                    .background(
                        if (isSelected) {
                            Color(0xFF7B4FBF).copy(alpha = 0.2f)
                        } else {
                            Color.White.copy(alpha = 0.15f)
                        }
                    )
                    .noRippleClickable {
                        onSelectKey(searchKey)
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isSelected) {
                        searchItem.selectedIcon
                    } else {
                        searchItem.icon
                    },
                    contentDescription = searchItem.label,
                    tint = if (isSelected) {
                        Color(0xFF7B4FBF)
                    } else {
                        Color(0xFF5C5C7A)
                    },
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
    val animatedBackground by animateColorAsState(
        targetValue = if (isSelected) {
            Color(0xFF7B4FBF).copy(alpha = 0.15f)
        } else {
            Color.Transparent
        },
        label = "nav_bg_color"
    )

    Row(
        modifier = Modifier
            .height(48.dp)
            .clip(RoundedCornerShape(50))
            .background(animatedBackground)
            .noRippleClickable(onClick)
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (isSelected) Color(0xFF7B4FBF) else Color(0xFF5C5C7A),
            modifier = Modifier.size(22.dp)
        )

        AnimatedVisibility(
            visible = isSelected
        ) {
            Text(
                text = label,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF7B4FBF),
                maxLines = 1
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
            .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
            .hazeEffect(
                state = hazeState,
                style = CupertinoMaterials.thin()
            )
            .background(Color.White.copy(alpha = 0.15f))
            .padding(horizontal = 16.dp)
            .padding(top = 12.dp, bottom = 8.dp + bottomPadding),
        horizontalArrangement = Arrangement.SpaceBetween,
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
    val animatedBackground by animateColorAsState(
        targetValue = if (isSelected) {
            Color(0xFF7B4FBF).copy(alpha = 0.1f)
        } else {
            Color.Transparent
        },
        label = "docked_nav_bg_color"
    )

    val animatedIconTint by animateColorAsState(
        targetValue = if (isSelected) Color(0xFF7B4FBF) else Color.Black.copy(alpha = 0.6f),
        label = "docked_nav_icon_tint"
    )

    Row(
        modifier = Modifier
            .height(48.dp)
            .clip(RoundedCornerShape(50))
            .background(animatedBackground)
            .noRippleClickable(onClick)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = animatedIconTint,
            modifier = Modifier.size(24.dp)
        )

        AnimatedVisibility(
            visible = isSelected
        ) {
            Text(
                text = label,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF7B4FBF),
                maxLines = 1
            )
        }
    }
}