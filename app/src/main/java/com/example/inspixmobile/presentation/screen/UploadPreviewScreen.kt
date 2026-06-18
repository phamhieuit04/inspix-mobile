package com.example.inspixmobile.presentation.screen

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.example.inspixmobile.presentation.component.BackButtonComponent
import com.example.inspixmobile.presentation.component.BackScaffold
import com.example.inspixmobile.presentation.viewmodel.UploadViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun UploadPreviewScreen(
    navigateToUploadSubmit: () -> Unit,
    navigateBack: () -> Unit,
    viewModel: UploadViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    BackHandler { navigateBack() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {

        AsyncImage(
            modifier = Modifier.fillMaxSize(),
            model = uiState.images.firstOrNull(),
            contentDescription = null
        )

        Row(
            modifier = Modifier
                .align(Alignment.TopStart)
                .fillMaxWidth()
                .statusBarsPadding(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            BackButtonComponent(
                iconBackgroundColor = Color(0xFFe8e8e9),
                iconTintColor = Color.DarkGray,
                onBackPressed = { navigateBack() }
            )


            Box(
                modifier = Modifier
                    .padding(top = 12.dp, start = 20.dp, end = 12.dp)
                    .shadow(
                        elevation = 6.dp,
                        shape = RoundedCornerShape(50),
                        clip = false
                    )
                    .background(
                        color = AccentPurple,
                        shape = RoundedCornerShape(50)
                    )
                    .clip(CircleShape)
                    .padding(14.dp)
                    .padding(horizontal = 8.dp)
                    .clickable(onClick = navigateToUploadSubmit),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Tiếp tục",
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}