package com.example.inspixmobile.presentation.screen

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.adamglin.PhosphorIcons
import com.composeunstyled.Text
import com.example.inspixmobile.core.extension.noRippleClickable
import com.example.inspixmobile.core.util.ImageHelper
import com.example.inspixmobile.presentation.viewmodel.DetailCollectionViewModel
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.materials.CupertinoMaterials
import dev.chrisbanes.haze.materials.ExperimentalHazeMaterialsApi
import dev.chrisbanes.haze.rememberHazeState
import org.koin.compose.viewmodel.koinViewModel
import androidx.core.graphics.toColorInt
import com.adamglin.phosphoricons.Bold
import com.adamglin.phosphoricons.bold.ArrowLeft
import com.adamglin.phosphoricons.bold.BookmarkSimple
import com.adamglin.phosphoricons.bold.Heart
import com.adamglin.phosphoricons.bold.Share
import com.example.inspixmobile.domain.model.Collection
import com.example.inspixmobile.presentation.component.ShimmerGridItem

@OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalHazeMaterialsApi::class
)
@Composable
fun DetailCollectionScreen(
    modifier: Modifier = Modifier,
    collection: Collection,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    navigateBack: () -> Unit,
    detailCollectionViewModel: DetailCollectionViewModel = koinViewModel()
) {
    val pagerState = rememberPagerState(
        initialPage = 0,
        pageCount = { collection.images?.count() ?: 0 }
    )
    val hazeState = rememberHazeState()
    val statusBarPadding = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()

    Box(
        modifier = modifier.fillMaxSize()
    ) {
        LazyVerticalStaggeredGrid(
            modifier = Modifier.fillMaxSize(),
            columns = StaggeredGridCells.Fixed(2),
            contentPadding = PaddingValues(
                top = statusBarPadding,
                bottom = 16.dp,
                start = 8.dp,
                end = 8.dp
            ),
            verticalItemSpacing = 8.dp,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item(span = StaggeredGridItemSpan.FullLine) {
                Box(
                    modifier = Modifier.fillMaxSize()
                ) {
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier
                            .fillMaxSize()
                            .aspectRatio(9f / 16f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.Transparent)
                    ) { page ->
                        val image = collection.images?.get(page)
                        val color = image?.color?.toColorInt()
                        val resolvedRatio = ImageHelper.aspectRatio(
                            image?.width,
                            image?.height
                        )

                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(color = Color(color!!))
                                .hazeSource(hazeState),
                            contentAlignment = Alignment.Center
                        ) {
                            with(sharedTransitionScope) {
                                AsyncImage(
                                    model = image.urlSmall,
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .aspectRatio(resolvedRatio)
                                        .sharedElement(
                                            sharedContentState = rememberSharedContentState(
                                                key = image.uuid!!
                                            ),
                                            animatedVisibilityScope = animatedVisibilityScope,
                                            boundsTransform = { _, _ ->
                                                spring(
                                                    dampingRatio = 0.75f,
                                                    stiffness = 380f
                                                )
                                            },
                                            clipInOverlayDuringTransition = OverlayClip(
                                                RoundedCornerShape(12.dp)
                                            )
                                        )
                                )
                            }
                        }
                    }

                    Row(
                        modifier = Modifier
                            .align(alignment = Alignment.BottomCenter)
                            .padding(bottom = 16.dp),
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Box(
                            modifier = Modifier
                                .hazeEffect(
                                    state = hazeState,
                                    style = CupertinoMaterials.thin()
                                )
                                .padding(14.dp)
                        ) {
                            Column() {
                                val author = collection.author

                                AsyncImage(
                                    model = author?.avatarUrl!!,
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .size(52.dp)
                                        .clip(CircleShape)
                                )

                                Row() {
                                    Text(
                                        text = author.name!!,
                                        color = Color.White
                                    )
                                    Text(
                                        text = author.bio!!,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                        Column() {
                            Box(
                                modifier = Modifier
                                    .padding(top = 12.dp, start = 20.dp)
                                    .clip(CircleShape)
                                    .noRippleClickable(onClick = {})
                                    .hazeEffect(
                                        state = hazeState,
                                        style = CupertinoMaterials.thin()
                                    )
                                    .padding(14.dp)
                            ) {
                                Icon(
                                    imageVector = PhosphorIcons.Bold.Heart,
                                    contentDescription = "Like",
                                    tint = Color.White
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .padding(top = 12.dp, start = 20.dp)
                                    .clip(CircleShape)
                                    .noRippleClickable(onClick = {})
                                    .hazeEffect(
                                        state = hazeState,
                                        style = CupertinoMaterials.thin()
                                    )
                                    .padding(14.dp)
                            ) {
                                Icon(
                                    imageVector = PhosphorIcons.Bold.BookmarkSimple,
                                    contentDescription = "Bookmark",
                                    tint = Color.White
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .padding(top = 12.dp, start = 20.dp)
                                    .clip(CircleShape)
                                    .noRippleClickable(onClick = {})
                                    .hazeEffect(
                                        state = hazeState,
                                        style = CupertinoMaterials.thin()
                                    )
                                    .padding(14.dp)
                            ) {
                                Icon(
                                    imageVector = PhosphorIcons.Bold.Share,
                                    contentDescription = "Share",
                                    tint = Color.White
                                )
                            }
                        }
                    }
                }
            }

            items(10) { key ->
                ShimmerGridItem(index = key)
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .statusBarsPadding()
                .padding(top = 12.dp, start = 20.dp)
                .clip(CircleShape)
                .noRippleClickable(navigateBack)
                .hazeEffect(
                    state = hazeState,
                    style = CupertinoMaterials.thin()
                )
                .padding(14.dp)
        ) {
            Icon(
                imageVector = PhosphorIcons.Bold.ArrowLeft,
                contentDescription = "Back",
                tint = Color.White
            )
        }
    }
}