package com.example.inspixmobile.presentation.screen

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.regular.ArrowLeft
import com.composeunstyled.Text
import com.example.inspixmobile.core.extension.noRippleClickable
import com.example.inspixmobile.core.util.ImageHelper
import com.example.inspixmobile.presentation.navigation.toTopLevelPageIndex
import com.example.inspixmobile.presentation.viewmodel.DetailCollectionViewModel
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.materials.CupertinoMaterials
import dev.chrisbanes.haze.materials.ExperimentalHazeMaterialsApi
import dev.chrisbanes.haze.rememberHazeState
import org.koin.compose.viewmodel.koinViewModel
import androidx.core.graphics.toColorInt

@OptIn(ExperimentalMaterial3Api::class, ExperimentalHazeMaterialsApi::class)
@Composable
fun DetailCollectionScreen(
    modifier: Modifier = Modifier,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    uuid: String,
    navigateBack: () -> Unit,
    detailCollectionViewModel: DetailCollectionViewModel = koinViewModel()
) {
    val collection by detailCollectionViewModel.getCollectionByUuid(uuid).collectAsState()

    val pagerState = rememberPagerState(
        initialPage = 0,
        pageCount = { collection?.images?.count() ?: 0 }
    )
    val hazeState = rememberHazeState()

    Box(
        modifier = modifier.fillMaxSize()
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .aspectRatio(9f / 16f)
                .fillMaxSize()
                .background(color = Color.Transparent)
                .hazeSource(hazeState)
        ) { page ->
            val image = collection?.images?.get(page)
            val color = image?.color!!.toColorInt()

            with(sharedTransitionScope) {
                AsyncImage(
                    model = image.urlSmall,
                    contentDescription = "",
                    modifier = Modifier
                        .fillMaxSize()
                        .sharedElement(
                            sharedContentState = sharedTransitionScope.rememberSharedContentState(
                                key = image.uuid!!
                            ),
                            animatedVisibilityScope = animatedVisibilityScope,
                            boundsTransform = { _, _ ->
                                spring(
                                    dampingRatio = 0.82f,
                                    stiffness = Spring.StiffnessMediumLow
                                )
                            },
                            clipInOverlayDuringTransition = OverlayClip(RoundedCornerShape(0.dp))
                        )
                        .background(color = Color(color))
                )
            }
        }

        Box(
            modifier = Modifier
                .noRippleClickable(navigateBack)
                .padding(16.dp)
                .align(Alignment.TopStart)
                .statusBarsPadding()
                .clip(CircleShape)
                .hazeEffect(state = hazeState, style = CupertinoMaterials.ultraThin())
                .padding(12.dp)
        ) {
            Icon(
                imageVector = PhosphorIcons.Regular.ArrowLeft,
                contentDescription = "Back",
                tint = Color.White
            )
        }
    }
}