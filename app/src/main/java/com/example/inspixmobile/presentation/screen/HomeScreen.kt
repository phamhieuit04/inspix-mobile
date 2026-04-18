package com.example.inspixmobile.presentation.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.example.inspixmobile.core.extension.noRippleClickable
import com.example.inspixmobile.core.extension.skeletonEffect
import com.example.inspixmobile.domain.model.Collection
import com.example.inspixmobile.domain.model.Image
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.materials.CupertinoMaterials
import dev.chrisbanes.haze.materials.ExperimentalHazeMaterialsApi
import dev.chrisbanes.haze.rememberHazeState

@Composable
fun HomeScreen(bottomContentPadding: Dp = 8.dp) {
    val collections = remember { fakeCollections() }
    val topics = listOf("All", "Nature", "Architecture", "Minimal", "Abstract", "People")
    var selectedTopic by remember { mutableStateOf("All") }
    val gridState = rememberLazyStaggeredGridState()
    var isSearchBarVisible by remember { mutableStateOf(true) }
    val hazeState = rememberHazeState()
    var headerHeightPx by remember { mutableIntStateOf(0) }
    val density = LocalDensity.current
    val headerHeightDp = with(density) { headerHeightPx.toDp() }

    LaunchedEffect(gridState) {
        var previousIndex = 0
        var previousOffset = 0
        snapshotFlow { gridState.firstVisibleItemIndex to gridState.firstVisibleItemScrollOffset }
            .collect { (currentIndex, currentOffset) ->
                val isScrollingDown =
                    currentIndex > previousIndex ||
                            (currentIndex == previousIndex && currentOffset > previousOffset)
                val isScrollingUp =
                    currentIndex < previousIndex ||
                            (currentIndex == previousIndex && currentOffset < previousOffset)
                when {
                    isScrollingDown && (currentIndex > 0 || currentOffset > 8) ->
                        isSearchBarVisible = false

                    isScrollingUp -> isSearchBarVisible = true
                }
                previousIndex = currentIndex
                previousOffset = currentOffset
            }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
    ) {
        LazyVerticalStaggeredGrid(
            columns = StaggeredGridCells.Fixed(2),
            state = gridState,
            contentPadding = PaddingValues(
                top = headerHeightDp + 8.dp,
                start = 8.dp,
                end = 8.dp,
                bottom = bottomContentPadding
            ),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalItemSpacing = 8.dp,
            modifier = Modifier
                .fillMaxSize()
                .hazeSource(state = hazeState)
        ) {
            items(collections) { collection ->
                CollectionCard(collection = collection)
            }
        }

        HomeHeader(
            modifier = Modifier.onSizeChanged { headerHeightPx = it.height },
            topics = topics,
            selectedTopic = selectedTopic,
            onTopicSelected = { selectedTopic = it },
            isSearchBarVisible = isSearchBarVisible,
            hazeState = hazeState
        )
    }
}

@OptIn(ExperimentalHazeMaterialsApi::class)
@Composable
private fun HomeHeader(
    modifier: Modifier = Modifier,
    topics: List<String>,
    selectedTopic: String,
    onTopicSelected: (String) -> Unit,
    isSearchBarVisible: Boolean,
    hazeState: HazeState,
) {
    val headerTransition =
        updateTransition(targetState = isSearchBarVisible, label = "home_header_transition")
    val topicTranslationY by headerTransition.animateFloat(
        transitionSpec = { tween(durationMillis = 220) },
        label = "topic_list_slide"
    ) { visible -> if (visible) 0f else -12f }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        AnimatedVisibility(
            visible = isSearchBarVisible,
            enter = fadeIn(animationSpec = tween(220)) +
                    expandVertically(animationSpec = tween(220), expandFrom = Alignment.Top),
            exit = fadeOut(animationSpec = tween(220)) +
                    shrinkVertically(animationSpec = tween(220), shrinkTowards = Alignment.Top)
        ) {
            Column {
                HomeSearchBar(hazeState = hazeState)
                Spacer(modifier = Modifier.height(12.dp))
            }
        }

        HomeTopicList(
            modifier = Modifier.graphicsLayer { translationY = topicTranslationY },
            topics = topics,
            selectedTopic = selectedTopic,
            onTopicSelected = onTopicSelected,
            hazeState = hazeState
        )
    }
}

@OptIn(ExperimentalHazeMaterialsApi::class)
@Composable
private fun HomeSearchBar(
    hazeState: HazeState,
    onSearchClick: () -> Unit = {},
    onFilterClick: () -> Unit = {},
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(50))
                .hazeEffect(state = hazeState, style = CupertinoMaterials.ultraThin())
                .background(Color.White.copy(alpha = 0.25f))
                .noRippleClickable { onSearchClick() }
                .padding(horizontal = 16.dp, vertical = 13.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.Search,
                contentDescription = null,
                tint = Color.Black.copy(alpha = 0.6f),
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = "Khám phá nghệ thuật...",
                color = Color.Black.copy(alpha = 0.6f),
                fontSize = 14.sp,
                fontWeight = FontWeight.Normal
            )
        }

        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .hazeEffect(state = hazeState, style = CupertinoMaterials.ultraThin())
                .background(Color.White.copy(alpha = 0.25f))
                .noRippleClickable { onFilterClick() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.Tune,
                contentDescription = "Filter",
                tint = Color.Black.copy(alpha = 0.6f),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@OptIn(ExperimentalHazeMaterialsApi::class)
@Composable
private fun HomeTopicList(
    modifier: Modifier = Modifier,
    topics: List<String>,
    selectedTopic: String,
    onTopicSelected: (String) -> Unit,
    hazeState: HazeState,
) {
    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(topics) { topic ->
            val isSelected = topic == selectedTopic
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .hazeEffect(state = hazeState, style = CupertinoMaterials.ultraThin())
                    .background(
                        if (isSelected) Color(0xFF7B4FBF).copy(alpha = 0.85f)
                        else Color.White.copy(alpha = 0.25f)
                    )
                    .noRippleClickable { onTopicSelected(topic) }
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = topic,
                    color = if (isSelected) Color.White else Color.Black.copy(alpha = 0.6f),
                    fontSize = 13.sp,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                )
            }
        }

        item {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .hazeEffect(state = hazeState, style = CupertinoMaterials.ultraThin())
                    .background(Color.White.copy(alpha = 0.2f))
                    .noRippleClickable { }
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Xem thêm",
                    color = Color(0xFF7B4FBF),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
fun CollectionCard(collection: Collection) {
    var isLiked by remember(collection.id) { mutableStateOf(collection.isLiked ?: false) }
    var isImageLoaded by remember(collection.id) { mutableStateOf(false) }
    val hasLoadErrorState = remember(collection.id) { mutableStateOf(false) }
    val firstImage = collection.images?.firstOrNull()
    val thumbnailUrl = firstImage?.urlRegular ?: firstImage?.urlSmall ?: firstImage?.urlFull

    if (hasLoadErrorState.value || thumbnailUrl == null) return

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (!isImageLoaded) Modifier.aspectRatio(3f / 4f) else Modifier)
            .clip(RoundedCornerShape(12.dp))
    ) {
        if (!isImageLoaded) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .skeletonEffect()
            )
        }

        AsyncImage(
            model = thumbnailUrl,
            contentDescription = collection.id?.toString(),
            contentScale = ContentScale.Crop,
            onLoading = { isImageLoaded = false },
            onSuccess = { isImageLoaded = true },
            onError = {
                isImageLoaded = false
                hasLoadErrorState.value = true
            },
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
        )

        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(8.dp)
                .size(36.dp)
                .background(Color.White.copy(alpha = 0.85f), CircleShape)
                .clickable { isLiked = !isLiked },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                contentDescription = null,
                tint = if (isLiked) Color(0xFFE53935) else Color(0xFF666666),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

fun fakeCollections(): List<Collection> {
    val heights = listOf(320, 340, 360, 380, 400, 420, 440, 460, 480, 500, 520, 540, 560, 580, 600)

    return List(50) { index ->
        val itemIndex = index + 1
        val imageCount = (3..5).random()
        val images = List(imageCount) { imageIndex ->
            val height = heights[(index + imageIndex) % heights.size]
            val imageUrl =
                "https://picsum.photos/seed/inspix-$itemIndex-${imageIndex + 1}/400/$height"

            Image(
                uuid = "img-$itemIndex-${imageIndex + 1}",
                userId = (itemIndex % 10).toLong() + 1L,
                collectionId = itemIndex.toLong(),
                urlSmall = imageUrl,
                urlRegular = imageUrl,
                urlFull = imageUrl,
                downloadUrl = imageUrl
            )
        }

        Collection(
            id = itemIndex.toLong(),
            userId = (itemIndex % 10).toLong() + 1L,
            topicId = (itemIndex % 6) + 1,
            title = "Collection $itemIndex",
            description = "Mock collection $itemIndex",
            isLiked = itemIndex % 4 == 0,
            totalLikes = (10..999).random(),
            images = images
        )
    }
}