package com.example.inspixmobile.presentation.screen

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculateCentroid
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.core.graphics.toColorInt
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.example.inspixmobile.core.util.ImageHelper
import com.example.inspixmobile.domain.model.Image
import com.example.inspixmobile.presentation.component.BackButton
import com.example.inspixmobile.presentation.component.BackScaffold
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

private const val DOUBLE_TAP_ZOOM_SCALE = 2f
private const val MAX_SCALE = 5f
private const val DOUBLE_TAP_TIMEOUT_MS = 300L

@Composable
fun ImagesViewer(
    images: List<Image>,
    initialPage: Int = 0,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    navigateBack: () -> Unit
) {
    val context = LocalContext.current
    val pagerState = rememberPagerState(
        initialPage = initialPage,
        pageCount = { images.size }
    )
    var zoomedPage by remember { mutableStateOf<Int?>(null) }

    BackHandler { navigateBack() }

    BackScaffold(
        sharedTransitionScope = sharedTransitionScope,
        isShowOverlayDelayed = false,
        onBackPressed = navigateBack
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize(),
            userScrollEnabled = zoomedPage == null,
            beyondViewportPageCount = 1
        ) { page ->
            val image = images[page]
            val imageKey = requireNotNull(image.uuid)
            val resolvedRatio = ImageHelper.aspectRatio(image.width, image.height)

            val scope = rememberCoroutineScope()
            val scale = remember { Animatable(1f) }
            val offset = remember { Animatable(Offset.Zero, Offset.VectorConverter) }
            var containerSize by remember { mutableStateOf(IntSize.Zero) }

            fun clampOffset(s: Float, raw: Offset): Offset {
                val maxX = ((s - 1f) * containerSize.width / 2f).coerceAtLeast(0f)
                val maxY = ((s - 1f) * containerSize.height / 2f).coerceAtLeast(0f)
                return Offset(raw.x.coerceIn(-maxX, maxX), raw.y.coerceIn(-maxY, maxY))
            }

            val iosSpring = spring<Float>(
                dampingRatio = Spring.DampingRatioNoBouncy,
                stiffness = 300f
            )
            val iosSpringOffset = spring<Offset>(
                dampingRatio = Spring.DampingRatioNoBouncy,
                stiffness = 300f
            )

            fun resetZoom() {
                scope.launch {
                    coroutineScope {
                        launch { scale.animateTo(1f, iosSpring) }
                        launch { offset.animateTo(Offset.Zero, iosSpringOffset) }
                    }
                    zoomedPage = null
                }
            }

            fun zoomToPoint(tapOffset: Offset) {
                scope.launch {
                    val w = containerSize.width.toFloat()
                    val h = containerSize.height.toFloat()
                    val s = DOUBLE_TAP_ZOOM_SCALE

                    val dx = tapOffset.x - w / 2f
                    val dy = tapOffset.y - h / 2f

                    val targetOffset = clampOffset(s, Offset(-dx * (s - 1f), -dy * (s - 1f)))

                    coroutineScope {
                        launch { scale.animateTo(s, iosSpring) }
                        launch { offset.animateTo(targetOffset, iosSpringOffset) }
                    }
                    zoomedPage = page
                }
            }

            with(sharedTransitionScope) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .onSizeChanged { containerSize = it }
                        .pointerInput(Unit) {
                            var lastTapTime = 0L
                            var lastTapPosition = Offset.Zero

                            awaitEachGesture {
                                val down = awaitFirstDown(requireUnconsumed = false)
                                val downTime = System.currentTimeMillis()
                                val downPosition = down.position

                                val isDoubleTap =
                                    (downTime - lastTapTime) < DOUBLE_TAP_TIMEOUT_MS &&
                                            (downPosition - lastTapPosition).getDistance() < 60f
                                lastTapTime = downTime
                                lastTapPosition = downPosition

                                if (isDoubleTap) {
                                    down.consume()
                                    if (scale.value > 1.01f) resetZoom() else zoomToPoint(
                                        downPosition
                                    )
                                    return@awaitEachGesture
                                }

                                do {
                                    val event = awaitPointerEvent()
                                    val touchCount = event.changes.count { it.pressed }
                                    if (touchCount == 0) break

                                    val zoomChange = event.calculateZoom()
                                    val panChange = event.calculatePan()
                                    val centroid = event.calculateCentroid()

                                    val isPinch = zoomChange != 1f
                                    val isPan = panChange != Offset.Zero && scale.value > 1.01f

                                    if (isPinch || isPan) {
                                        val prevScale = scale.value
                                        val newScale =
                                            (prevScale * zoomChange).coerceIn(1f, MAX_SCALE)

                                        val centroidX = centroid.x - containerSize.width / 2f
                                        val centroidY = centroid.y - containerSize.height / 2f
                                        val scaleDiff = newScale - prevScale
                                        val focalDelta = Offset(
                                            -centroidX * scaleDiff / newScale.coerceAtLeast(0.01f),
                                            -centroidY * scaleDiff / newScale.coerceAtLeast(0.01f)
                                        )

                                        val raw = offset.value + panChange + focalDelta
                                        val clamped = clampOffset(newScale, raw)

                                        scope.launch {
                                            scale.snapTo(newScale)
                                            offset.snapTo(clamped)
                                        }
                                        event.changes.forEach { it.consume() }

                                        if (newScale > 1.01f) zoomedPage = page
                                        else if (newScale <= 1.01f) zoomedPage = null
                                    }
                                } while (true)

                                when {
                                    scale.value < 1.05f -> resetZoom()
                                    else -> {
                                        val clamped = clampOffset(scale.value, offset.value)
                                        if (clamped != offset.value) {
                                            scope.launch {
                                                offset.animateTo(clamped, iosSpringOffset)
                                            }
                                        }
                                    }
                                }
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer {
                                scaleX = scale.value
                                scaleY = scale.value
                                translationX = offset.value.x
                                translationY = offset.value.y
                            }
                            .background(Color.Black),
                        contentAlignment = Alignment.Center
                    ) {
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(image.urlSmall)
                                .memoryCacheKey(imageKey)
                                .placeholderMemoryCacheKey(image.urlSmall)
                                .crossfade(false)
                                .build(),
                            contentDescription = null,
                            contentScale = ContentScale.Fit,
                            modifier = Modifier
                                .fillMaxSize()
                                .aspectRatio(resolvedRatio)
                                .sharedElement(
                                    sharedContentState = rememberSharedContentState(key = imageKey),
                                    animatedVisibilityScope = animatedVisibilityScope,
                                    boundsTransform = { _, _ ->
                                        spring(
                                            dampingRatio = 0.85f,
                                            stiffness = Spring.StiffnessLow
                                        )
                                    },
                                    clipInOverlayDuringTransition = OverlayClip(RoundedCornerShape(0.dp)),
                                    renderInOverlayDuringTransition = true,
                                    zIndexInOverlay = 0f
                                )
                        )

                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(image.urlFull)
                                .crossfade(false)
                                .build(),
                            contentDescription = null,
                            contentScale = ContentScale.Fit,
                            modifier = Modifier
                                .fillMaxSize()
                                .aspectRatio(resolvedRatio)
                        )
                    }
                }
            }
        }
    }
}