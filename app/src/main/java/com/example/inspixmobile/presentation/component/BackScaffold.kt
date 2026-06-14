package com.example.inspixmobile.presentation.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.EnterExitState
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Bold
import com.adamglin.phosphoricons.bold.ArrowLeft

@Composable
fun BackScaffold(
    modifier: Modifier = Modifier,
    sharedTransitionScope: SharedTransitionScope,
    backgroundColor: Color = Color(0xFFe8e8e9),
    iconColor: Color = Color.DarkGray,
    isShowOverlayDelayed: Boolean = true,
    onBackPressed: () -> Unit,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(color = Color.Transparent)
    ) {
        content()

        if (isShowOverlayDelayed) {
            with(sharedTransitionScope) {
                AnimatedVisibility(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .statusBarsPadding()
                        .renderInSharedTransitionScopeOverlay(zIndexInOverlay = 2f),
                    visible = true,
                    enter = EnterTransition.None,
                    exit = ExitTransition.None
                ) {
                    BackButton(
                        backgroundColor = backgroundColor,
                        iconColor = iconColor,
                        onBackPressed = onBackPressed
                    )
                }
            }
        } else {
            BackButton(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .statusBarsPadding(),
                backgroundColor = backgroundColor,
                iconColor = iconColor,
                onBackPressed = onBackPressed
            )
        }
    }
}

@Composable
fun BackButton(
    modifier: Modifier = Modifier,
    backgroundColor: Color,
    iconColor: Color,
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
                color = backgroundColor,
                shape = CircleShape
            )
            .clip(CircleShape)
            .clickable(onClick = onBackPressed)
            .padding(14.dp)
    ) {
        Icon(
            imageVector = PhosphorIcons.Bold.ArrowLeft,
            contentDescription = "Back",
            tint = iconColor
        )
    }
}