package com.example.inspixmobile.presentation.screen

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.SharedTransitionScope
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridState
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.paging.compose.collectAsLazyPagingItems
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Bold
import com.adamglin.phosphoricons.bold.Gear
import com.composeunstyled.Icon
import com.composeunstyled.Text
import com.example.inspixmobile.core.extension.formatCompact
import com.example.inspixmobile.core.util.ImageHelper
import com.example.inspixmobile.domain.model.Collection
import com.example.inspixmobile.domain.model.User
import com.example.inspixmobile.presentation.component.BackScaffold
import com.example.inspixmobile.presentation.component.CollectionCardComponent
import com.example.inspixmobile.presentation.component.ProfileHeaderComponent
import com.example.inspixmobile.presentation.component.StatItemComponent
import com.example.inspixmobile.presentation.viewmodel.DetailArtistViewModel
import com.example.inspixmobile.presentation.viewmodel.ProfileViewModel
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import kotlin.collections.get

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
    val density = LocalDensity.current
    val context = LocalContext.current

    val artistCollections = detailArtistViewModel.artistCollections.collectAsLazyPagingItems()

    val statusBarHeight = WindowInsets.statusBars
        .asPaddingValues()
        .calculateTopPadding()

    val gridState = rememberSaveable(
        saver = LazyStaggeredGridState.Saver
    ) {
        LazyStaggeredGridState()
    }

    val interactions by detailArtistViewModel.interactions.collectAsState()

    LaunchedEffect(artist.uuid) {
        detailArtistViewModel.setUserUuid(artist.uuid!!)
    }

    BackHandler { navigateBack() }

    BackScaffold(
        sharedTransitionScope = sharedTransitionScope,
        onBackPressed = { navigateBack() },
    ) {
        LazyVerticalStaggeredGrid(
            modifier = Modifier.fillMaxSize(),
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