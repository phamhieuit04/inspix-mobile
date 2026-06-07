package com.example.inspixmobile.presentation.screen

import androidx.annotation.DrawableRes
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Bold
import com.adamglin.phosphoricons.bold.ArrowLeft
import com.adamglin.phosphoricons.bold.SignOut
import com.example.inspixmobile.R
import com.example.inspixmobile.presentation.component.LayoutToggleComponent
import com.example.inspixmobile.presentation.component.NavigationBarStyle
import com.example.inspixmobile.presentation.component.TopShadowOverlay
import com.example.inspixmobile.presentation.viewmodel.SettingViewModel
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingScreen(
    bottomContentPadding: Dp = 8.dp,
    layoutStyle: HomeLayoutStyle,
    navbarStyle: NavigationBarStyle,
    onBackPressed: () -> Unit,
    onLogout: () -> Unit,
    settingViewModel: SettingViewModel = koinInject()
) {
    val density = LocalDensity.current

    val backgroundColor = Color(0xFFe8e8e9)
    val iconColor = Color.DarkGray

    var headerHeightPx by remember { mutableIntStateOf(0) }
    val headerHeightDp = with(density) { headerHeightPx.toDp() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color = Color(0xFFF0F0F5))
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(
                top = headerHeightDp + 52.dp,
                bottom = bottomContentPadding + 32.dp
            )
        ) {
            item {
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Giao diện".uppercase(),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp)
                )

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    SettingToggleRow(
                        title = "Bố cục bài đăng",
                        action = {
                            LayoutToggleComponent(
                                layoutStyle = layoutStyle,
                                onClick = { settingViewModel.updateHomeLayout(it) }
                            )
                        }
                    )

                    NavigationBarStyleSelector(
                        navbarStyle = navbarStyle,
                        onNavbarStyle = { settingViewModel.updateNavbarLayout(it) }
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))
            }

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(72.dp)
                        .background(color = Color(0xfffbf9ff), shape = RoundedCornerShape(28.dp))
                        .clip(RoundedCornerShape(28.dp))
                        .clickable(onClick = onLogout)
                        .padding(horizontal = 18.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        modifier = Modifier.weight(1f),
                        text = "Đăng xuất",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFFE53935)
                    )

                    Icon(
                        imageVector = PhosphorIcons.Bold.SignOut,
                        contentDescription = null,
                        tint = Color(0xFFE53935),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        SettingHeader(
            title = "Cài đặt",
            backgroundColor = backgroundColor,
            iconColor = iconColor,
            onBackPressed = onBackPressed,
            modifier = Modifier
                .statusBarsPadding()
                .onSizeChanged({ headerHeightPx = it.height })
        )
    }
}

@Composable
private fun SettingHeader(
    modifier: Modifier = Modifier,
    title: String,
    backgroundColor: Color,
    iconColor: Color,
    onBackPressed: () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(color = Color(0xFFF0F0F5))
    ) {
        Box(
            modifier = Modifier
                .padding(top = 12.dp, start = 20.dp)
                .background(color = backgroundColor, shape = CircleShape)
                .clip(CircleShape)
                .clickable(onClick = onBackPressed)
                .padding(14.dp)
                .align(Alignment.TopStart)
        ) {
            Icon(
                imageVector = PhosphorIcons.Bold.ArrowLeft,
                contentDescription = "Back",
                tint = iconColor
            )
        }

        Text(
            modifier = Modifier
                .align(alignment = Alignment.Center)
                .padding(top = 12.dp),
            text = title,
            fontSize = 22.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun SettingToggleRow(
    title: String,
    action: @Composable () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp)
            .background(color = Color(0xfffbf9ff), shape = RoundedCornerShape(28.dp))
            .clip(RoundedCornerShape(28.dp))
            .padding(horizontal = 18.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            modifier = Modifier.weight(1f),
            text = title,
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF1C1C1E)
        )

        action()
    }
}

@Composable
private fun NavigationBarStyleSelector(
    navbarStyle: NavigationBarStyle,
    onNavbarStyle: (NavigationBarStyle) -> Unit
) {
    val purple = Color(0xFF7B4FBF)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
    ) {
        NavigationBarStyleCard(
            title = "Nổi",
            imageRes = R.drawable.floating_navbar,
            selected = navbarStyle == NavigationBarStyle.Floating,
            onClick = { onNavbarStyle(NavigationBarStyle.Floating) },
            purple = purple
        )

        NavigationBarStyleCard(
            title = "Gắn liền",
            imageRes = R.drawable.docked_navbar,
            selected = navbarStyle == NavigationBarStyle.Docked,
            onClick = { onNavbarStyle(NavigationBarStyle.Docked) },
            purple = purple
        )
    }
}

@Composable
private fun NavigationBarStyleCard(
    title: String,
    @DrawableRes imageRes: Int,
    selected: Boolean,
    onClick: () -> Unit,
    purple: Color
) {
    val bgColor by animateColorAsState(
        targetValue = if (selected) purple.copy(alpha = 0.06f) else Color.Transparent,
        animationSpec = tween(200),
        label = "bgColor"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(bgColor)
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(18.dp)
                    .clip(CircleShape)
                    .background(if (selected) purple else Color.Transparent)
                    .border(
                        width = 2.dp,
                        color = if (selected) purple else Color(0xFFBBBBBB),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (selected) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                    )
                }
            }

            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                color = if (selected) purple else Color(0xFF6E6E6E)
            )
        }

        Image(
            painter = painterResource(imageRes),
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp)
                .clip(RoundedCornerShape(10.dp))
                .border(
                    width = 1.dp,
                    color = Color(0xFFE5E5EA),
                    shape = RoundedCornerShape(10.dp)
                ),
            contentScale = ContentScale.FillWidth
        )


        Spacer(modifier = Modifier.height(14.dp))
    }
}