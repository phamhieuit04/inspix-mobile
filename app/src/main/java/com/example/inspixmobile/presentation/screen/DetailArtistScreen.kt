package com.example.inspixmobile.presentation.screen

import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridState
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import com.example.inspixmobile.core.extension.formatCompact
import com.example.inspixmobile.core.util.ImageHelper
import com.example.inspixmobile.domain.model.Collection
import com.example.inspixmobile.domain.model.User
import com.example.inspixmobile.presentation.component.BackScaffold
import com.example.inspixmobile.presentation.component.CollectionCardComponent
import com.example.inspixmobile.presentation.component.EmptyCollectionState
import com.example.inspixmobile.presentation.component.ProfileHeaderComponent
import com.example.inspixmobile.presentation.component.ShimmerGridItem
import com.example.inspixmobile.presentation.component.StatItemComponent
import com.example.inspixmobile.presentation.viewmodel.DetailArtistViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun DetailArtistScreen(
    artist: User,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    bottomContentPadding: Dp = 8.dp,
    navigateBack: () -> Unit,
    navigateToDetailCollection: (Collection) -> Unit,
    detailArtistViewModel: DetailArtistViewModel = koinViewModel()
) {
    val context = LocalContext.current

    val artistCollections = detailArtistViewModel.artistCollections.collectAsLazyPagingItems()
    val pullToRefreshState = rememberPullToRefreshState()
    val interactions by detailArtistViewModel.interactions.collectAsState()

    val isRefreshing = artistCollections.loadState.refresh is LoadState.Loading
    var isUserRefreshing by remember { mutableStateOf(false) }
    val isShimmering = isRefreshing && (artistCollections.itemCount == 0 || isUserRefreshing)

    val statusBarHeight = WindowInsets.statusBars
        .asPaddingValues()
        .calculateTopPadding()

    val gridState = rememberSaveable(saver = LazyStaggeredGridState.Saver) {
        LazyStaggeredGridState()
    }

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
        PullToRefreshBox(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF0F0F5)),
            state = pullToRefreshState,
            isRefreshing = isRefreshing,
            onRefresh = {
                isUserRefreshing = true
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
                columns = StaggeredGridCells.Fixed(2),
                verticalItemSpacing = 8.dp,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(
                    top = statusBarHeight + 16.dp,
                    start = 8.dp,
                    end = 8.dp,
                    bottom = bottomContentPadding + 16.dp
                )
            ) {
                item(span = StaggeredGridItemSpan.FullLine) {
                    Header(
                        modifier = Modifier.statusBarsPadding(),
                        user = artist
                    )
                }

                item(span = StaggeredGridItemSpan.FullLine) {
                    Spacer(modifier = Modifier.padding(top = 8.dp))
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

                            val interaction = interactions[collection.uuid]
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

@Composable
private fun ArtistStatsRow(
    totalCollections: Int = 0,
    totalFollowers: Int = 0,
    totalFollowings: Int = 0
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center
    ) {
        StatItemComponent(
            value = totalCollections.formatCompact(),
            label = "Bộ sưu tập"
        )

        Spacer(modifier = Modifier.width(48.dp))

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
    modifier: Modifier = Modifier,
    user: User? = null,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        ProfileHeaderComponent(
            name = user?.name.orEmpty(),
            avatar = user?.avatarUrl.orEmpty(),
            bio = user?.bio.orEmpty()
        )

        ArtistStatsRow(
            totalCollections = user?.totalCollections ?: 0,
            totalFollowers = user?.followers ?: 0,
            totalFollowings = user?.following ?: 0
        )
    }
}