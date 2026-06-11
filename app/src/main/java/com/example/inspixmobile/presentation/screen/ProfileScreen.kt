package com.example.inspixmobile.presentation.screen

import android.util.Log
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridState
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.SecondaryIndicator
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Bold
import com.adamglin.phosphoricons.bold.Gear
import com.adamglin.phosphoricons.bold.ImageSquare
import com.composeunstyled.Icon
import com.composeunstyled.Text
import com.example.inspixmobile.core.extension.formatCompact
import com.example.inspixmobile.core.extension.skeletonEffect
import com.example.inspixmobile.core.util.ImageHelper
import com.example.inspixmobile.domain.model.Collection
import com.example.inspixmobile.domain.model.User
import com.example.inspixmobile.presentation.component.CollectionCardComponent
import com.example.inspixmobile.presentation.component.ShimmerProfileScreen
import com.example.inspixmobile.presentation.viewmodel.ProfileViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ProfileScreen(
    modifier: Modifier = Modifier,
    uuid: String,
    bottomContentPadding: Dp = 8.dp,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    scrollToTopSignal: Int,
    navigateToSetting: () -> Unit,
    navigateToDetail: (Collection) -> Unit = {},
    profileViewModel: ProfileViewModel = koinViewModel()
) {
    val density = LocalDensity.current
    val context = LocalContext.current

    val user by profileViewModel.user.collectAsState()
    val ownedCollections = profileViewModel.ownedCollections.collectAsLazyPagingItems()
    val likedCollections = profileViewModel.likedCollections.collectAsLazyPagingItems()
    val isRefreshing by profileViewModel.isRefreshing.collectAsState()

    val pullToRefreshState = rememberPullToRefreshState()

    val statusBarHeight = WindowInsets.statusBars
        .asPaddingValues()
        .calculateTopPadding()

    var headerHeightPx by remember { mutableIntStateOf(0) }
    val headerHeightDp = with(density) { headerHeightPx.toDp() }

    var selectedTabIndex by rememberSaveable {
        mutableIntStateOf(0)
    }
    val tabs = listOf("Của tui", "Đã thích")

    var lastScrollToTopSignal by rememberSaveable { mutableIntStateOf(0) }
    val gridState = rememberSaveable(
        saver = LazyStaggeredGridState.Saver
    ) {
        LazyStaggeredGridState()
    }

    val interactions by profileViewModel.interactions.collectAsState()

    LaunchedEffect(uuid) {
        profileViewModel.setUserUuid(uuid)
    }

    LaunchedEffect(scrollToTopSignal) {
        if (scrollToTopSignal > lastScrollToTopSignal) {
            gridState.animateScrollToItem(0)
            lastScrollToTopSignal = scrollToTopSignal
        }
    }

    PullToRefreshBox(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF0F0F5)),
        state = pullToRefreshState,
        isRefreshing = isRefreshing,
        onRefresh = {
            profileViewModel.refresh()
            likedCollections.refresh()
        },
        indicator = {
            PullToRefreshDefaults.Indicator(
                state = pullToRefreshState,
                isRefreshing = isRefreshing,
                modifier = Modifier.align(Alignment.TopCenter)
            )
        }
    ) {
        AnimatedContent(
            targetState = isRefreshing
        ) { state ->
            if (state) {
                ShimmerProfileScreen(
                    headerHeightDp = headerHeightDp,
                    bottomContentPadding = bottomContentPadding
                )
            } else {
                LazyVerticalStaggeredGrid(
                    state = gridState,
                    columns = StaggeredGridCells.Fixed(2),
                    modifier = Modifier.fillMaxSize(),
                    verticalItemSpacing = 8.dp,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(
                        start = 8.dp,
                        end = 8.dp,
                        bottom = bottomContentPadding + 16.dp
                    )
                ) {
                    item(span = StaggeredGridItemSpan.FullLine) {
                        Header(
                            statusBarHeight = statusBarHeight,
                            onHeaderHeightChanged = { headerHeightPx = it },
                            navigateToSetting = navigateToSetting,
                            user = user
                        )
                    }

                    item(span = StaggeredGridItemSpan.FullLine) {
                        Spacer(modifier = Modifier.height(24.dp))

                        CollectionTabs(
                            selectedTabIndex = selectedTabIndex,
                            tabs = tabs,
                            onTabSelected = { selectedTabIndex = it }
                        )
                    }

                    if (selectedTabIndex == 0) {
                        if (ownedCollections.itemCount == 0) {
                            item(span = StaggeredGridItemSpan.FullLine) {
                                EmptyCollectionState()
                            }
                        } else {
                            items(
                                count = ownedCollections.itemCount,
                                key = ownedCollections.itemKey {
                                    it.uuid ?: it.hashCode().toString()
                                }
                            ) { index ->
                                val collection = ownedCollections[index] ?: return@items

                                val coverImage = collection.images?.firstOrNull()
                                val resolvedRatio = ImageHelper.aspectRatio(
                                    coverImage?.width,
                                    coverImage?.height
                                )

                                val interaction = interactions[collection.uuid]
                                val isLiked =
                                    interaction?.isLiked ?: (collection.isLiked ?: false)

                                CollectionCardComponent(
                                    modifier = Modifier.animateItem(),
                                    context = context,
                                    sharedTransitionScope = sharedTransitionScope,
                                    animatedVisibilityScope = animatedVisibilityScope,
                                    collection = collection,
                                    aspectRatio = resolvedRatio,
                                    isLiked = isLiked,
                                    likeButtonVisible = true,
                                    onClick = { navigateToDetail(collection) },
                                    onToggleLike = { profileViewModel.toggleLike(collection) }
                                )
                            }
                        }
                    } else {
                        if (likedCollections.itemCount == 0) {
                            item(key = "empty_liked", span = StaggeredGridItemSpan.FullLine) {
                                EmptyCollectionState()
                            }
                        } else {
                            items(
                                count = likedCollections.itemCount,
                                key = likedCollections.itemKey {
                                    it.uuid ?: it.hashCode().toString()
                                }
                            ) { index ->
                                val collection = likedCollections[index] ?: return@items

                                val coverImage = collection.images?.firstOrNull()
                                val resolvedRatio = ImageHelper.aspectRatio(
                                    coverImage?.width,
                                    coverImage?.height
                                )

                                val interaction = interactions[collection.uuid]
                                val isLiked =
                                    interaction?.isLiked ?: (collection.isLiked ?: false)

                                CollectionCardComponent(
                                    modifier = Modifier.animateItem(),
                                    context = context,
                                    sharedTransitionScope = sharedTransitionScope,
                                    animatedVisibilityScope = animatedVisibilityScope,
                                    collection = collection,
                                    aspectRatio = resolvedRatio,
                                    isLiked = isLiked,
                                    likeButtonVisible = true,
                                    onClick = { navigateToDetail(collection) },
                                    onToggleLike = { profileViewModel.toggleLike(collection) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CollectionTabs(
    selectedTabIndex: Int,
    tabs: List<String>,
    onTabSelected: (Int) -> Unit
) {
    PrimaryTabRow(
        selectedTabIndex = selectedTabIndex,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)),
        containerColor = Color.Transparent,
        contentColor = Color(0xFF7B4FBF),
        indicator = {
            TabRowDefaults.PrimaryIndicator(
                modifier = Modifier.tabIndicatorOffset(selectedTabIndex),
                width = Dp.Unspecified,
                height = 3.dp,
                color = Color(0xFF7B4FBF),
                shape = RoundedCornerShape(topStart = 3.dp, topEnd = 3.dp)
            )
        },
        divider = {}
    ) {
        tabs.forEachIndexed { index, title ->
            Tab(
                selected = selectedTabIndex == index,
                onClick = { onTabSelected(index) },
                text = {
                    Text(
                        text = title,
                        fontSize = 14.sp,
                        fontWeight = if (selectedTabIndex == index) FontWeight.SemiBold else FontWeight.Normal,
                        color = if (selectedTabIndex == index) Color(0xFF7B4FBF) else Color(
                            0xFF888888
                        )
                    )
                }
            )
        }
    }
}

@Composable
private fun EmptyCollectionState() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 64.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            imageVector = PhosphorIcons.Bold.ImageSquare,
            contentDescription = null,
            modifier = Modifier.size(48.dp),
            tint = Color(0xFFCCCCCC)
        )
        Text(
            text = "Chưa có bộ sưu tập nào",
            fontSize = 15.sp,
            color = Color(0xFFAAAAAA),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun ProfileHeader(name: String, avatar: String, bio: String) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(148.dp)
                .background(
                    color = Color(0xFF7B4FBF),
                    shape = CircleShape
                )
                .padding(4.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.background)
                    .padding(3.dp),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = avatar,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = name,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onSurface
        )

        if (bio.isNotBlank() && !bio.equals("null", true)) {
            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = bio,
                fontSize = 14.sp,
                color = Color(0xFF666666),
                lineHeight = 22.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun StatsRow(totalCollections: Int = 0, totalLikes: Int = 0, totalImages: Int = 0) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center
    ) {
        StatItem(
            value = totalCollections.formatCompact(),
            label = "Bộ sưu tập"
        )

        Spacer(modifier = Modifier.width(48.dp))

        StatItem(
            value = totalLikes.formatCompact(),
            label = "Yêu thích"
        )

        Spacer(modifier = Modifier.width(48.dp))

        StatItem(
            value = totalImages.formatCompact(),
            label = "Hình ảnh"
        )
    }
}

@Composable
private fun StatItem(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun Header(
    statusBarHeight: Dp,
    onHeaderHeightChanged: (Int) -> Unit,
    navigateToSetting: () -> Unit,
    user: User? = null
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            IconButton(
                modifier = Modifier
                    .align(alignment = Alignment.TopEnd)
                    .padding(top = statusBarHeight + 8.dp, end = 8.dp)
                    .onSizeChanged { onHeaderHeightChanged(it.height) },
                onClick = navigateToSetting
            ) {
                Icon(
                    imageVector = PhosphorIcons.Bold.Gear,
                    contentDescription = null
                )
            }
        }

        ProfileHeader(
            name = user?.name.orEmpty(),
            avatar = user?.avatarUrl.orEmpty(),
            bio = user?.bio.orEmpty()
        )

        StatsRow(
            totalCollections = user?.totalCollections ?: 0,
            totalLikes = user?.totalLikes ?: 0,
            totalImages = user?.totalImages ?: 0
        )
    }
}