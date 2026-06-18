package com.example.inspixmobile.presentation.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

@Composable
fun BackScaffold(
    modifier: Modifier = Modifier,
    sharedTransitionScope: SharedTransitionScope,
    iconBackgroundColor: Color = Color(0xFFe8e8e9),
    iconTintColor: Color = Color.DarkGray,
    isShowOverlayDelayed: Boolean = true,
    onBackPressed: () -> Unit,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Transparent)
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
                    BackButtonComponent(
                        backgroundColor = iconBackgroundColor,
                        iconColor = iconTintColor,
                        onBackPressed = onBackPressed
                    )
                }
            }
        } else {
            BackButtonComponent(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .statusBarsPadding(),
                backgroundColor = iconBackgroundColor,
                iconColor = iconTintColor,
                onBackPressed = onBackPressed
            )
        }
    }
}