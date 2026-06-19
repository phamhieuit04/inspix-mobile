package com.example.inspixmobile.presentation.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Bold
import com.adamglin.phosphoricons.bold.ArrowLeft

@Composable
fun BackButtonComponent(
    modifier: Modifier = Modifier,
    iconBackgroundColor: Color,
    iconTintColor: Color,
    onBackPressed: () -> Unit
) {
    Box(
        modifier = modifier
            .padding(top = 12.dp, start = 20.dp)
            .shadow(
                elevation = 6.dp,
                shape = CircleShape,
                clip = false
            )
            .background(
                color = iconBackgroundColor,
                shape = CircleShape
            )
            .clip(CircleShape)
            .clickable(onClick = onBackPressed)
            .padding(14.dp)
    ) {
        Icon(
            imageVector = PhosphorIcons.Bold.ArrowLeft,
            contentDescription = "Back",
            tint = iconTintColor
        )
    }
}