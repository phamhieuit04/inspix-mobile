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
    val collectionInteractions by detailArtistViewModel.collectionInteractions.collectAsState()
    val userInteractions by detailArtistViewModel.userInteractions.collectAsState()

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
                    start = 8.dp,
                    end = 8.dp,
                    bottom = bottomContentPadding + 16.dp
                )
            ) {
                item(span = StaggeredGridItemSpan.FullLine) {
                    val interaction = userInteractions[artist.uuid!!]
                    val isFollowed = interaction?.isFollowed ?: (artist.isFollowed ?: false)

                    Header(
                        statusBarHeight = statusBarHeight,
                        user = artist,
                        isFollowed = isFollowed,
                        onFollow = { detailArtistViewModel.toggleFollow(artist) }
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

        FollowButton(
            isFollowed = isFollowed,
            onClick = onFollow
        )

        ArtistStatsRow(
            totalFollowers = user?.followers ?: 0,
            totalFollowings = user?.following ?: 0
        )
    }
}

@Composable
private fun FollowButton(
    isFollowed: Boolean,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(80.dp)

    val backgroundColor by animateColorAsState(
        targetValue = if (isFollowed) {
            Color(0xFFF3F4F6)
        } else {
            AccentPurple
        },
        animationSpec = tween(durationMillis = 250),
        label = "backgroundColor"
    )

    val contentColor by animateColorAsState(
        targetValue = if (isFollowed) {
            Color(0xFF334155)
        } else {
            Color.White
        },
        animationSpec = tween(durationMillis = 250),
        label = "contentColor"
    )

    val borderColor by animateColorAsState(
        targetValue = if (isFollowed) {
            Color.LightGray
        } else {
            Color.Transparent
        },
        animationSpec = tween(durationMillis = 250),
        label = "borderColor"
    )

    val scale by animateFloatAsState(
        targetValue = if (isFollowed) 1.03f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "scale"
    )

    Button(
        onClick = onClick,
        modifier = Modifier
            .height(56.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .border(
                width = if (isFollowed) 2.dp else 0.dp,
                color = borderColor,
                shape = shape
            ),
        shape = shape,
        colors = ButtonDefaults.buttonColors(
            containerColor = backgroundColor
        ),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 0.dp
        )
    ) {
        AnimatedContent(
            targetState = isFollowed,
            transitionSpec = {
                (
                        fadeIn(
                            animationSpec = tween(220)
                        ) + slideInVertically(
                            initialOffsetY = { it / 2 }
                        )
                        ) togetherWith (
                        fadeOut(
                            animationSpec = tween(180)
                        ) + slideOutVertically(
                            targetOffsetY = { -it / 2 }
                        )
                        )
            },
            label = "followState"
        ) { followed ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (followed) {
                        PhosphorIcons.Bold.Check
                    } else {
                        PhosphorIcons.Bold.Plus
                    },
                    contentDescription = null,
                    tint = contentColor
                )

                Text(
                    text = if (followed) {
                        "Đang theo dõi"
                    } else {
                        "Theo dõi"
                    },
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = contentColor
                )
            }
        }
    }
}