package com.example.inspixmobile.presentation.screen

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.toColorInt
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Bold
import com.adamglin.phosphoricons.Fill
import com.adamglin.phosphoricons.bold.ArrowDown
import com.adamglin.phosphoricons.bold.ChatCircle
import com.adamglin.phosphoricons.bold.Heart
import com.adamglin.phosphoricons.fill.Heart
import com.composeunstyled.Text
import com.example.inspixmobile.core.extension.formatCompact
import com.example.inspixmobile.core.extension.noRippleClickable
import com.example.inspixmobile.core.util.ImageHelper
import com.example.inspixmobile.domain.model.Image
import com.example.inspixmobile.presentation.component.BackScaffold
import com.example.inspixmobile.presentation.viewmodel.UploadViewModel
import org.koin.compose.viewmodel.koinViewModel
import kotlin.text.equals

@Composable
fun UploadSubmitScreen(
    sharedTransitionScope: SharedTransitionScope,
    bottomContentPadding: Dp = 8.dp,
    navigateToImagesViewer: (List<Image>, Int) -> Unit,
    navigateBack: () -> Unit,
    viewModel: UploadViewModel = koinViewModel()
) {
    val context = LocalContext.current

    val uiState by viewModel.uiState.collectAsState()
    val images = uiState.images

    val statusBarPadding = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val backgroundColor = Color(0xFFe8e8e9)
    val iconColor = Color.DarkGray

    val pagerState = rememberPagerState(
        initialPage = 0,
        pageCount = { images.size }
    )

    BackHandler { navigateBack() }

    BackScaffold(
        modifier = Modifier.background(Color(0xFFF0F0F5)),
        sharedTransitionScope = sharedTransitionScope,
        onBackPressed = {
            navigateBack()
        }
    ) {
        LazyColumn(
            contentPadding = PaddingValues(
                top = statusBarPadding,
                bottom = bottomContentPadding,
                start = 8.dp,
                end = 8.dp
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            item {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier
                        .fillMaxSize()
                        .aspectRatio(3f / 4f)
                        .background(color = Color.Black, shape = RoundedCornerShape(12.dp))
                        .clip(RoundedCornerShape(12.dp)),
                ) { page ->
                    val image = images[page]
                    val imageKey = requireNotNull(image.uuid)

                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(image.uri)
                            .memoryCacheKey(imageKey)
                            .crossfade(true)
                            .build(),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .clickable(onClick = {
                                navigateToImagesViewer(images, page)
                            })
                    )
                }
            }
        }
    }
}