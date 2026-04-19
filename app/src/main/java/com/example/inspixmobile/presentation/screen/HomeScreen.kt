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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material.icons.outlined.ViewAgenda
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
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.example.inspixmobile.core.extension.noRippleClickable
import com.example.inspixmobile.core.extension.skeletonEffect
import com.example.inspixmobile.domain.model.Collection
import com.example.inspixmobile.domain.model.Image
import com.example.inspixmobile.domain.model.User
import com.example.inspixmobile.presentation.viewmodel.HomeViewModel
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.materials.CupertinoMaterials
import dev.chrisbanes.haze.materials.ExperimentalHazeMaterialsApi
import dev.chrisbanes.haze.rememberHazeState
import org.koin.compose.viewmodel.koinViewModel

enum class HomeLayoutStyle { Grid, Feed }

@Composable
fun HomeScreen(
    bottomContentPadding: Dp = 8.dp,
    homeViewModel: HomeViewModel = koinViewModel()
) {
    val collections = remember { fakeCollections() }
    val topics = listOf("All", "Nature", "Architecture", "Minimal", "Abstract", "People")
    var selectedTopic by remember { mutableStateOf("All") }
    var isSearchBarVisible by remember { mutableStateOf(true) }
    var layoutStyle by remember { mutableStateOf(HomeLayoutStyle.Grid) }
    val hazeState = rememberHazeState()
    var headerHeightPx by remember { mutableIntStateOf(0) }
    val density = LocalDensity.current
    val headerHeightDp = with(density) { headerHeightPx.toDp() }
    val gridState = rememberLazyStaggeredGridState()
    val feedState = rememberLazyListState()

    LaunchedEffect(gridState) {
        var previousIndex = 0
        var previousOffset = 0
        snapshotFlow { gridState.firstVisibleItemIndex to gridState.firstVisibleItemScrollOffset }
            .collect { (currentIndex, currentOffset) ->
                val isScrollingDown = currentIndex > previousIndex ||
                        (currentIndex == previousIndex && currentOffset > previousOffset)
                val isScrollingUp = currentIndex < previousIndex ||
                        (currentIndex == previousIndex && currentOffset < previousOffset)
                when {
                    isScrollingDown && (currentIndex > 0 || currentOffset > 8) -> isSearchBarVisible =
                        false

                    isScrollingUp -> isSearchBarVisible = true
                }
                previousIndex = currentIndex
                previousOffset = currentOffset
            }
    }

    LaunchedEffect(feedState) {
        var previousIndex = 0
        var previousOffset = 0
        snapshotFlow { feedState.firstVisibleItemIndex to feedState.firstVisibleItemScrollOffset }
            .collect { (currentIndex, currentOffset) ->
                val isScrollingDown = currentIndex > previousIndex ||
                        (currentIndex == previousIndex && currentOffset > previousOffset)
                val isScrollingUp = currentIndex < previousIndex ||
                        (currentIndex == previousIndex && currentOffset < previousOffset)
                when {
                    isScrollingDown && (currentIndex > 0 || currentOffset > 8) -> isSearchBarVisible =
                        false

                    isScrollingUp -> isSearchBarVisible = true
                }
                previousIndex = currentIndex
                previousOffset = currentOffset
            }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF0F0F5))
    ) {
        when (layoutStyle) {
            HomeLayoutStyle.Grid -> LazyVerticalStaggeredGrid(
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
                items(collections) { collection -> CollectionCard(collection = collection) }
            }

            HomeLayoutStyle.Feed -> LazyColumn(
                state = feedState,
                contentPadding = PaddingValues(
                    top = headerHeightDp + 8.dp,
                    bottom = bottomContentPadding
                ),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .hazeSource(state = hazeState)
            ) {
                items(collections) { collection -> CollectionFeedCard(collection = collection) }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0xFFF0F0F5), Color.Transparent)
                    )
                )
        )

        HomeHeader(
            modifier = Modifier.onSizeChanged { headerHeightPx = it.height },
            topics = topics,
            selectedTopic = selectedTopic,
            onTopicSelected = { selectedTopic = it },
            isSearchBarVisible = isSearchBarVisible,
            hazeState = hazeState,
            layoutStyle = layoutStyle,
            onLayoutToggle = {
                layoutStyle =
                    if (layoutStyle == HomeLayoutStyle.Grid) HomeLayoutStyle.Feed else HomeLayoutStyle.Grid
            }
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
    layoutStyle: HomeLayoutStyle,
    onLayoutToggle: () -> Unit,
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
            .statusBarsPadding()
            .padding(vertical = 12.dp)
    ) {
        AnimatedVisibility(
            visible = isSearchBarVisible,
            enter = fadeIn(animationSpec = tween(220)) +
                    expandVertically(animationSpec = tween(220), expandFrom = Alignment.Top),
            exit = fadeOut(animationSpec = tween(220)) +
                    shrinkVertically(animationSpec = tween(220), shrinkTowards = Alignment.Top)
        ) {
            Column {
                HomeSearchBar(
                    hazeState = hazeState,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .graphicsLayer { translationY = topicTranslationY }
                .padding(start = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            LayoutToggleButton(
                layoutStyle = layoutStyle,
                hazeState = hazeState,
                onClick = onLayoutToggle
            )

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(end = 16.dp)
            ) {
                items(topics) { topic ->
                    val isSelected = topic == selectedTopic
                    Box(
                        modifier = Modifier
                            .widthIn(min = 80.dp)
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
                            .widthIn(min = 80.dp)
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
    }
}

@OptIn(ExperimentalHazeMaterialsApi::class)
@Composable
private fun LayoutToggleButton(
    layoutStyle: HomeLayoutStyle,
    hazeState: HazeState,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .hazeEffect(state = hazeState, style = CupertinoMaterials.ultraThin())
            .background(Color.White.copy(alpha = 0.2f))
            .noRippleClickable { onClick() }
            .padding(vertical = 4.dp, horizontal = 4.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(if (layoutStyle == HomeLayoutStyle.Grid) Color.White else Color.Transparent)
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.GridView,
                    contentDescription = "Grid layout",
                    tint = if (layoutStyle == HomeLayoutStyle.Grid) Color(0xFF7B4FBF) else Color.Black.copy(
                        alpha = 0.4f
                    ),
                    modifier = Modifier.size(16.dp)
                )
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(if (layoutStyle == HomeLayoutStyle.Feed) Color.White else Color.Transparent)
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.ViewAgenda,
                    contentDescription = "Feed layout",
                    tint = if (layoutStyle == HomeLayoutStyle.Feed) Color(0xFF7B4FBF) else Color.Black.copy(
                        alpha = 0.4f
                    ),
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalHazeMaterialsApi::class)
@Composable
private fun HomeSearchBar(
    hazeState: HazeState,
    modifier: Modifier = Modifier,
    onSearchClick: () -> Unit = {},
    onFilterClick: () -> Unit = {},
) {
    Row(
        modifier = modifier,
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
            onError = { isImageLoaded = false; hasLoadErrorState.value = true },
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

@Composable
fun CollectionFeedCard(collection: Collection) {
    var isLiked by remember(collection.id) { mutableStateOf(collection.isLiked ?: false) }
    val images = collection.images.orEmpty()
    val displayImages = images.take(3)
    val totalImages = images.size
    val hasMore = totalImages > 3
    val pageCount = if (hasMore) displayImages.size + 1 else displayImages.size
    val pagerState = rememberPagerState(pageCount = { pageCount })
    val showAllBgImage = images.getOrNull(3) ?: images.getOrNull(2)
    val isOnShowAllPage = hasMore && pagerState.currentPage == displayImages.size

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF7B4FBF).copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                val avatarUrl = collection.author?.avatarUrl
                if (avatarUrl != null) {
                    AsyncImage(
                        model = avatarUrl,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                    )
                } else {
                    Text(
                        text = collection.author?.name?.take(1)?.uppercase() ?: "U",
                        color = Color(0xFF7B4FBF),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(1.dp)
            ) {
                Text(
                    text = collection.author?.name ?: "Unknown",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF1A1A2E),
                    lineHeight = 16.sp
                )
                if (!collection.title.isNullOrBlank()) {
                    Text(
                        text = collection.title,
                        fontSize = 11.sp,
                        color = Color(0xFF888899),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        lineHeight = 14.sp
                    )
                }
            }
        }

        Box(modifier = Modifier.fillMaxWidth()) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxWidth()
            ) { page ->
                if (hasMore && page == displayImages.size) {
                    val bgUrl = showAllBgImage?.urlRegular
                        ?: showAllBgImage?.urlSmall
                        ?: showAllBgImage?.urlFull

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        if (bgUrl != null) {
                            AsyncImage(
                                model = bgUrl,
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .blur(100.dp)
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color(0xFF2A1A4A))
                            )
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.3f))
                        )

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(Color.White.copy(alpha = 0.25f))
                                .clickable { }
                                .padding(horizontal = 24.dp, vertical = 12.dp)
                        ) {
                            Text(
                                text = "Xem tất cả $totalImages ảnh",
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                } else {
                    val image = displayImages[page]
                    val imageUrl = image.urlRegular ?: image.urlSmall ?: image.urlFull
                    var isLoaded by remember(imageUrl) { mutableStateOf(false) }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1f)
                    ) {
                        if (!isLoaded) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .skeletonEffect()
                            )
                        }

                        AsyncImage(
                            model = imageUrl,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            onSuccess = { isLoaded = true },
                            onError = { isLoaded = true },
                            modifier = Modifier.fillMaxSize()
                        )

                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(10.dp)
                                .clip(RoundedCornerShape(50))
                                .background(Color.Black.copy(alpha = 0.4f))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "${page + 1}/$totalImages",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            this@Column.AnimatedVisibility(
                visible = !isOnShowAllPage,
                enter = fadeIn(animationSpec = tween(200)),
                exit = fadeOut(animationSpec = tween(200)),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
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

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = collection.description ?: "",
                fontSize = 12.sp,
                color = Color(0xFF444455),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )

            Text(
                text = collection.createdAt ?: "",
                fontSize = 11.sp,
                color = Color(0xFF888899),
                textAlign = TextAlign.End,
                modifier = Modifier.padding(start = 8.dp)
            )
        }
    }
}

fun fakeCollections(): List<Collection> {
    val heights = listOf(320, 340, 360, 380, 400, 420, 440, 460, 480, 500, 520, 540, 560, 580, 600)
    val authorNames =
        listOf("Akira", "Yuki", "Hana", "Ren", "Sora", "Miku", "Taro", "Nana", "Kenji", "Aoi")
    val descriptions = listOf(
        "A journey through light", "Silent moments", "Urban dreams",
        "Nature's palette", "Abstract thoughts", "Color symphony",
        "Whispers of dawn", "Digital canvas", "Lost in time", "Vivid echoes"
    )
    val timeAgo =
        listOf("2 phút trước", "15 phút trước", "1 giờ trước", "3 giờ trước", "1 ngày trước")

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
            description = descriptions[index % descriptions.size],
            isLiked = itemIndex % 4 == 0,
            totalLikes = (10..999).random(),
            images = images,
            author = User(
                id = (itemIndex % 10).toLong() + 1L,
                name = authorNames[index % authorNames.size],
                avatarUrl = "https://picsum.photos/seed/avatar-${itemIndex % 10}/100/100"
            ),
            createdAt = timeAgo[index % timeAgo.size]
        )
    }
}