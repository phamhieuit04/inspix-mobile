package com.example.inspixmobile.presentation.component

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Bold
import com.adamglin.phosphoricons.bold.Check
import com.adamglin.phosphoricons.bold.Plus
import com.example.inspixmobile.presentation.screen.AccentPurple

@Composable
fun FollowButtonComponent(
    isFollowed: Boolean = false,
    size: FollowButtonSize = FollowButtonSize.Medium,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(80.dp)

    val backgroundColor by animateColorAsState(
        targetValue = if (isFollowed) Color(0xFFF3F4F6) else AccentPurple,
        animationSpec = tween(durationMillis = 250),
        label = "backgroundColor"
    )

    val contentColor by animateColorAsState(
        targetValue = if (isFollowed) Color(0xFF334155) else Color.White,
        animationSpec = tween(durationMillis = 250),
        label = "contentColor"
    )

    val borderColor by animateColorAsState(
        targetValue = if (isFollowed) Color.LightGray else Color.Transparent,
        animationSpec = tween(durationMillis = 250),
        label = "borderColor"
    )

    val scale by animateFloatAsState(
        targetValue = if (isFollowed) 1.03f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "scale"
    )

    Button(
        onClick = onClick,
        modifier = Modifier
            .height(size.height)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .border(
                width = if (isFollowed) 2.dp else 0.dp,
                color = borderColor,
                shape = shape
            ),
        shape = shape,
        colors = ButtonDefaults.buttonColors(
            containerColor = backgroundColor
        ),
        contentPadding = PaddingValues(horizontal = size.horizontalPadding),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
    ) {
        AnimatedContent(
            targetState = isFollowed,
            transitionSpec = {
                (
                        fadeIn(animationSpec = tween(220)) +
                                slideInVertically(initialOffsetY = { it / 2 })
                        ) togetherWith (
                        fadeOut(animationSpec = tween(180)) +
                                slideOutVertically(targetOffsetY = { -it / 2 })
                        )
            },
            label = "followState"
        ) { followed ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (followed) PhosphorIcons.Bold.Check else PhosphorIcons.Bold.Plus,
                    contentDescription = null,
                    tint = contentColor,
                    modifier = Modifier.size(size.iconSize)
                )

                Text(
                    text = if (followed) "Đang theo dõi" else "Theo dõi",
                    fontSize = size.fontSize,
                    fontWeight = FontWeight.SemiBold,
                    color = contentColor
                )
            }
        }
    }
}

enum class FollowButtonSize(
    val height: Dp,
    val horizontalPadding: Dp,
    val iconSize: Dp,
    val fontSize: TextUnit
) {
    Small(height = 42.dp, horizontalPadding = 12.dp, iconSize = 14.dp, fontSize = 12.sp),
    Medium(height = 56.dp, horizontalPadding = 24.dp, iconSize = 16.dp, fontSize = 14.sp),
    Large(height = 64.dp, horizontalPadding = 32.dp, iconSize = 20.dp, fontSize = 16.sp)
}