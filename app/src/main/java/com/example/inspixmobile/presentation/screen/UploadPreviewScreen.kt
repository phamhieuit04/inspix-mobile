package com.example.inspixmobile.presentation.screen

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.example.inspixmobile.presentation.component.BackScaffold
import com.example.inspixmobile.presentation.viewmodel.UploadViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun UploadPreviewScreen(
    sharedTransitionScope: SharedTransitionScope,
    bottomContentPadding: Dp = 8.dp,
    navigateBack: () -> Unit,
    viewModel: UploadViewModel = koinViewModel()
) {

    val uiState by viewModel.uiState.collectAsState()

    BackScaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        sharedTransitionScope = sharedTransitionScope,
        onBackPressed = {
            viewModel.clearImages()
            navigateBack()
        }
    ) {
        AsyncImage(
            modifier = Modifier.fillMaxSize(),
            model = uiState.images.firstOrNull(),
            contentDescription = null
        )
    }
}