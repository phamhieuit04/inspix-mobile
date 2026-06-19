package com.example.inspixmobile.presentation.screen

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridState
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Bold
import com.adamglin.phosphoricons.bold.Check
import com.adamglin.phosphoricons.bold.Plus
import com.example.inspixmobile.core.extension.formatCompact
import com.example.inspixmobile.core.util.ImageHelper
import com.example.inspixmobile.domain.model.Collection
import com.example.inspixmobile.domain.model.User
import com.example.inspixmobile.presentation.component.BackScaffold
import com.example.inspixmobile.presentation.component.CollectionCardComponent
import com.example.inspixmobile.presentation.component.EmptyCollectionState
import com.example.inspixmobile.presentation.component.FollowButtonComponent
import com.example.inspixmobile.presentation.component.ProfileHeaderComponent
import com.example.inspixmobile.presentation.component.ShimmerGridItem
import com.example.inspixmobile.presentation.component.StatItemComponent
import com.example.inspixmobile.presentation.viewmodel.DetailArtistViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun DetailArtistScreen(
    artist: User,
    isTablet: Boolean,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    bottomContentPadding: Dp = 8.dp,
    navigateBack: () -> Unit,
    navigateToDetailCollection: (Collection) -> Unit,
    detailArtistViewModel: DetailArtistViewModel = koinViewModel()
) {
    val context = LocalContext.current

    val artistCollections = detailArtistViewModel.artistCollections.collectAsLazyPagingItems()
    val collectionInteractions by detailArtistViewModel.collectionInteractions.collectAsState()
    val userInteractions by detailArtistViewModel.userInteractions.collectAsState()

    val user by detailArtistViewModel.user.collectAsState()
    val currentArtist = user ?: artist

    val pullToRefreshState = rememberPullToRefreshState()
    val isRefreshing = artistCollections.loadState.refresh is LoadState.Loading
    var isUserRefreshing by remember { mutableStateOf(false) }
    val isShimmering = isRefreshing && (artistCollections.itemCount == 0 || isUserRefreshing)

    val statusBarHeight = WindowInsets.statusBars
        .asPaddingValues()
        .calculateTopPadding()

    val gridState = rememberSaveable(saver = LazyStaggeredGridState.Saver) {
        LazyStaggeredGridState()
    }

    val columns = if (isTablet) 3 else 2

    LaunchedEffect(isRefreshing) {
        if (!isRefreshing) isUserRefreshing = false
    }

    LaunchedEffect(artist.uuid) {
        detailArtistViewModel.setUserUuid(artist.uuid!!)
    }

    BackHandler { navigateBack() }

    BackScaffold(
        sharedTransitionScope = sharedTransitionScope,
        onBackPressed = { navigateBack() },
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF0F0F5)),
            contentAlignment = Alignment.TopCenter
        ) {
            PullToRefreshBox(
                modifier = Modifier
                    .widthIn(max = 600.dp)
                    .fillMaxSize(),
                state = pullToRefreshState,
                isRefreshing = isRefreshing,
                onRefresh = {
                    isUserRefreshing = true
                    detailArtistViewModel.refresh()
                    artistCollections.refresh()
                },
                indicator = {
                    PullToRefreshDefaults.Indicator(
                        state = pullToRefreshState,
                        isRefreshing = isRefreshing,
                        modifier = Modifier.align(Alignment.TopCenter)
                    )
                }
            ) {
                LazyVerticalStaggeredGrid(
                    state = gridState,
                    columns = StaggeredGridCells.Fixed(columns),
                    verticalItemSpacing = 8.dp,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(
                        start = 8.dp,
                        end = 8.dp,
                        bottom = bottomContentPadding + 16.dp
                    )
                ) {
                    item(span = StaggeredGridItemSpan.FullLine) {
                        val interaction = userInteractions[currentArtist.uuid!!]
                        val isFollowed =
                            interaction?.isFollowed ?: (currentArtist.isFollowed ?: false)

                        Header(
                            statusBarHeight = statusBarHeight,
                            user = currentArtist,
                            isFollowed = isFollowed,
                            onFollow = { detailArtistViewModel.toggleFollow(currentArtist) }
                        )
                    }

                    item(span = StaggeredGridItemSpan.FullLine) {
                        Spacer(modifier = Modifier.padding(top = 16.dp))
                    }

                    if (isShimmering) {
                        items(count = 6) { index ->
                            ShimmerGridItem(index = index)
                        }
                    } else {
                        if (artistCollections.itemCount == 0 &&
                            artistCollections.loadState.refresh is LoadState.NotLoading
                        ) {
                            item(key = "empty", span = StaggeredGridItemSpan.FullLine) {
                                EmptyCollectionState()
                            }
                        } else {
                            items(count = artistCollections.itemCount) { index ->
                                val collection = artistCollections[index] ?: return@items

                                val coverImage = collection.images?.firstOrNull()
                                val resolvedRatio = ImageHelper.aspectRatio(
                                    coverImage?.width,
                                    coverImage?.height
                                )

                                val interaction = collectionInteractions[collection.uuid]
                                val isLiked =
                                    interaction?.isLiked ?: (collection.isLiked ?: false)

                                CollectionCardComponent(
                                    context = context,
                                    collection = collection,
                                    aspectRatio = resolvedRatio,
                                    isLiked = isLiked,
                                    sharedTransitionScope = sharedTransitionScope,
                                    animatedVisibilityScope = animatedVisibilityScope,
                                    onClick = { navigateToDetailCollection(collection) },
                                    onToggleLike = { detailArtistViewModel.toggleLike(collection) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ArtistStatsRow(
    totalFollowers: Int = 0,
    totalFollowings: Int = 0
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center
    ) {
        StatItemComponent(
            value = totalFollowers.formatCompact(),
            label = "Người theo dõi"
        )

        Spacer(modifier = Modifier.width(48.dp))

        StatItemComponent(
            value = totalFollowings.formatCompact(),
            label = "Đang theo dõi"
        )
    }
}

@Composable
private fun Header(
    statusBarHeight: Dp,
    user: User? = null,
    isFollowed: Boolean = false,
    onFollow: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp)
            .padding(top = statusBarHeight + 8.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        ProfileHeaderComponent(
            name = user?.name.orEmpty(),
            avatar = user?.avatarUrl.orEmpty(),
            bio = user?.bio.orEmpty()
        )

        FollowButtonComponent(
            isFollowed = isFollowed,
            onClick = onFollow
        )

        ArtistStatsRow(
            totalFollowers = user?.followers ?: 0,
            totalFollowings = user?.following ?: 0
        )
    }
}