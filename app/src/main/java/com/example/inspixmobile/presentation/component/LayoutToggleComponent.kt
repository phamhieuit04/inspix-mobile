package com.example.inspixmobile.presentation.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.ViewAgenda
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.inspixmobile.core.extension.noRippleClickable
import com.example.inspixmobile.presentation.screen.HomeLayoutStyle
import dev.chrisbanes.haze.materials.ExperimentalHazeMaterialsApi


@OptIn(ExperimentalHazeMaterialsApi::class)
@Composable
fun LayoutToggleComponent(
    layoutStyle: HomeLayoutStyle,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(color = Color(0xFFF0F0F5))
            .noRippleClickable { onClick() }
            .padding(vertical = 4.dp, horizontal = 4.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(if (layoutStyle == HomeLayoutStyle.Grid) Color.White else Color.Transparent)
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.GridView,
                    contentDescription = "Grid layout",
                    tint = if (layoutStyle == HomeLayoutStyle.Grid) Color(0xFF7B4FBF) else Color.Black.copy(
                        alpha = 0.4f
                    ),
                    modifier = Modifier.size(16.dp)
                )
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(if (layoutStyle == HomeLayoutStyle.Feed) Color.White else Color.Transparent)
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.ViewAgenda,
                    contentDescription = "Feed layout",
                    tint = if (layoutStyle == HomeLayoutStyle.Feed) Color(0xFF7B4FBF) else Color.Black.copy(
                        alpha = 0.4f
                    ),
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}