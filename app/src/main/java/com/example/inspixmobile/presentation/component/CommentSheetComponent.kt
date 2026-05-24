package com.example.inspixmobile.presentation.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.composables.core.DragIndication
import com.composables.core.ModalBottomSheet
import com.composables.core.Scrim
import com.composables.core.Sheet
import com.composables.core.SheetDetent
import com.composables.core.rememberModalBottomSheetState
import com.composeunstyled.Text
import com.composeunstyled.TextField
import com.example.inspixmobile.core.extension.noRippleClickable
import com.example.inspixmobile.presentation.viewmodel.CommentSheetViewModel
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommentSheetComponent(
    commentSheetViewModel: CommentSheetViewModel = koinViewModel()
) {
    val context = LocalContext.current

    val uiState by commentSheetViewModel.uiState.collectAsState()
    val comments = uiState.comments

    val Peek = SheetDetent(identifier = "peek") { containerHeight, sheetHeight ->
        containerHeight * 0.6f
    }

    val sheetState = rememberModalBottomSheetState(
        initialDetent = SheetDetent.Hidden,
        detents = listOf(
            SheetDetent.Hidden,
            Peek,
            SheetDetent.FullyExpanded
        )
    )

    LaunchedEffect(uiState.visible) {
        sheetState.targetDetent = if (uiState.visible) {
            Peek
        } else {
            SheetDetent.Hidden
        }
    }

    LaunchedEffect(sheetState.currentDetent) {
        if (sheetState.currentDetent == SheetDetent.Hidden && uiState.visible) {
            commentSheetViewModel.hide()
        }
    }

    ModalBottomSheet(state = sheetState) {
        Scrim(
            modifier = Modifier.noRippleClickable(
                onClick = { commentSheetViewModel.hide() })
        )

        Sheet(
            modifier = Modifier
                .padding(top = 48.dp)
                .shadow(4.dp, RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                .background(MaterialTheme.colorScheme.surface)
                .fillMaxSize()
                .imePadding()
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            text = "Comments",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    },
                    navigationIcon = {
                        Spacer(modifier = Modifier.width(48.dp))
                    },
                    actions = { },
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(alignment = Alignment.TopStart)
                )

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 8.dp, bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp)
                ) {
                    items(comments) { comment ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            val user = comment.user
                            val avatar = user?.avatarUrl
                            val avatarLoadError = remember(comment.id) {
                                mutableStateOf(false)
                            }

                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFCCCCCC)),
                                contentAlignment = Alignment.Center
                            ) {
                                if (avatar != null && !avatarLoadError.value) {
                                    AsyncImage(
                                        model = ImageRequest.Builder(context)
                                            .data(avatar)
                                            .size(88)
                                            .crossfade(true)
                                            .build(),
                                        contentDescription = null,
                                        contentScale = ContentScale.Crop,
                                        onError = { avatarLoadError.value = true },
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .clip(CircleShape)
                                    )
                                } else {
                                    Text(
                                        text = user?.name?.take(1)?.uppercase()
                                            ?: "U",
                                        color = Color(0xFF7B4FBF),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                            Text(
                                text = comment.content ?: "",
                                fontSize = 14.sp,
                                color = Color(0xFF333333)
                            )
                        }
                    }
                }

                TextField(
                    value = "",
                    onValueChange = { },
                    modifier = Modifier.align(alignment = Alignment.BottomStart)
                ) {
                    Text(
                        text = "Add a comment...",
                        color = Color(0xFF888888),
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}