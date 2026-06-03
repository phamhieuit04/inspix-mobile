package com.example.inspixmobile.presentation.screen

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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.composeunstyled.Text
import com.example.inspixmobile.core.extension.formatCompact
import com.example.inspixmobile.domain.model.User

@Composable
fun ProfileScreen(
    modifier: Modifier = Modifier,
    bottomContentPadding: Dp = 0.dp,
) {
    val user = User(
        uuid = "054b057a-c11b-49b2-986b-fc3159a1867a",
        name = "Lucas Miller",
        email = "user8@inspix.local",
        bio = "I love collecting visual ideas for product and branding projects.",
        avatarUrl = "http://100.107.16.50:8000/uploads/avatars/avatar-54.jpg",
        totalCollections = 152,
        totalLikes = 12351245,
        totalImages = 12445
    )

    val pullToRefreshState = rememberPullToRefreshState()

    val statusBarHeight = WindowInsets.statusBars
        .asPaddingValues()
        .calculateTopPadding()

    PullToRefreshBox(
        modifier = modifier.fillMaxSize(),
        state = pullToRefreshState,
        isRefreshing = false,
        onRefresh = { },
        indicator = {
            PullToRefreshDefaults.Indicator(
                state = pullToRefreshState,
                isRefreshing = false,
                modifier = Modifier.align(Alignment.TopCenter)
            )
        }
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    start = 16.dp,
                    end = 16.dp,
                ),
            verticalArrangement = Arrangement.spacedBy(24.dp),
            contentPadding = PaddingValues(
                top = statusBarHeight + 24.dp,
                bottom = bottomContentPadding
            )
        ) {
            item(key = "header") {
                ProfileHeader(
                    name = user.name.orEmpty(),
                    avatar = user.avatarUrl.orEmpty(),
                    bio = user.bio.orEmpty()
                )
            }

            item(key = "stats") {
                StatsRow(
                    totalCollections = user.totalCollections ?: 0,
                    totalLikes = user.totalLikes ?: 0,
                    totalImages = user.totalImages ?: 0
                )
            }
        }
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