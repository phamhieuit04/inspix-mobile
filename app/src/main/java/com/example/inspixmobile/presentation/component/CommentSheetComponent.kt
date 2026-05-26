package com.example.inspixmobile.presentation.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Bold
import com.adamglin.phosphoricons.bold.ArrowBendUpLeft
import com.adamglin.phosphoricons.bold.PaperPlaneRight
import com.adamglin.phosphoricons.bold.X
import com.composables.core.ModalBottomSheet
import com.composables.core.Scrim
import com.composables.core.Sheet
import com.composables.core.SheetDetent
import com.composables.core.rememberModalBottomSheetState
import com.composeunstyled.Text
import com.example.inspixmobile.core.extension.noRippleClickable
import com.example.inspixmobile.domain.model.Comment
import com.example.inspixmobile.presentation.viewmodel.CommentSheetViewModel
import kotlinx.coroutines.delay
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommentSheetComponent(
    viewModel: CommentSheetViewModel = koinViewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val uiState by viewModel.uiState.collectAsState()

    val comments = uiState.comments
    val rootComments = remember(comments) { comments.filter { it.parentId == null } }
    val repliesMap = remember(comments) {
        comments.filter { it.parentId != null }.groupBy { it.parentId }
    }
    val inputText = uiState.inputText
    val replyingTo = uiState.replyingTo

    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    val sheetState = rememberModalBottomSheetState(
        initialDetent = SheetDetent.Hidden,
        detents = listOf(SheetDetent.Hidden, SheetDetent.FullyExpanded)
    )

    val backgroundColor = Color(0xFFe8e8e9)
    val iconColor = Color.DarkGray

    LaunchedEffect(uiState.visible) {
        sheetState.targetDetent =
            if (uiState.visible) SheetDetent.FullyExpanded else SheetDetent.Hidden
    }

    LaunchedEffect(sheetState.currentDetent) {
        if (sheetState.currentDetent == SheetDetent.Hidden && uiState.visible) {
            viewModel.hide()
        }
    }

    LaunchedEffect(replyingTo) {
        if (replyingTo != null) {
            delay(80)
            try {
                focusRequester.requestFocus()
            } catch (_: Exception) {
            }
            delay(80)
            keyboardController?.show()
        }
    }

    ModalBottomSheet(state = sheetState) {
        Scrim(modifier = Modifier.noRippleClickable { viewModel.hide() })

        Sheet(
            modifier = Modifier
                .padding(top = 48.dp)
                .shadow(4.dp, RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                .background(MaterialTheme.colorScheme.surface)
                .fillMaxSize()
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                Column(modifier = Modifier.fillMaxSize()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Bình luận",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f)
                        )
                        if (comments.isNotEmpty()) {
                            Box(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(Color(0xFF222222))
                                    .padding(horizontal = 9.dp, vertical = 4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = comments.size.toString(),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.White
                                )
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                        }
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(color = backgroundColor)
                                .clickable(onClick = { viewModel.hide() }),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = PhosphorIcons.Bold.X,
                                contentDescription = "Đóng",
                                tint = iconColor,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    HorizontalDivider(color = Color(0xFFF0F0F0), thickness = 1.dp)

                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(0.dp)
                    ) {
                        if (rootComments.isEmpty()) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 48.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "Chưa có bình luận nào",
                                        fontSize = 14.sp,
                                        color = Color(0xFF999999)
                                    )
                                }
                            }
                        } else {
                            items(rootComments, key = { it.id ?: it.hashCode() }) { comment ->
                                val replies = repliesMap[comment.id].orEmpty()

                                CommentItem(
                                    context = context,
                                    comment = comment,
                                    onReply = { viewModel.setReplyingTo(comment) }
                                )

                                if (replies.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Column(
                                        modifier = Modifier.padding(start = 48.dp),
                                        verticalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        replies.forEach { reply ->
                                            CommentItem(
                                                context = context,
                                                comment = reply,
                                                isReply = true,
                                                onReply = { viewModel.setReplyingTo(comment) }
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(20.dp))
                            }
                        }
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .background(MaterialTheme.colorScheme.surface)
                        .windowInsetsPadding(WindowInsets.ime.union(WindowInsets.navigationBars))
                ) {
                    HorizontalDivider(color = Color(0xFFF0F0F0), thickness = 1.dp)

                    if (replyingTo != null) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFF7B4FBF).copy(alpha = 0.1f))
                                .padding(horizontal = 20.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = PhosphorIcons.Bold.ArrowBendUpLeft,
                                    contentDescription = null,
                                    tint = Color(0xFF7B4FBF),
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = "Đang trả lời ${replyingTo.user?.name ?: "người dùng"}",
                                    fontSize = 13.sp,
                                    color = Color(0xFF7B4FBF),
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .noRippleClickable { viewModel.clearReplyingTo() },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = PhosphorIcons.Bold.X,
                                    contentDescription = "Hủy reply",
                                    tint = Color(0xFF7B4FBF),
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(28.dp))
                                .background(Color(0xFF7B4FBF).copy(alpha = 0.1f))
                                .padding(horizontal = 18.dp, vertical = 14.dp)
                        ) {
                            BasicTextField(
                                value = inputText,
                                onValueChange = { viewModel.updateInputText(it) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .focusRequester(focusRequester),
                                textStyle = TextStyle(
                                    fontSize = 15.sp,
                                    color = Color(0xFF111111),
                                    lineHeight = 22.sp
                                ),
                                cursorBrush = SolidColor(Color(0xFF7B4FBF)),
                                decorationBox = { inner ->
                                    if (inputText.isEmpty()) {
                                        Text(
                                            text = "Thêm bình luận...",
                                            fontSize = 15.sp,
                                            color = Color(0xFFAAAAAA)
                                        )
                                    }
                                    inner()
                                }
                            )
                        }

                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(
                                    if (inputText.isNotBlank()) Color(0xFF7B4FBF)
                                    else Color(0xFF7B4FBF).copy(alpha = 0.15f)
                                )
                                .noRippleClickable {
                                    if (inputText.isNotBlank()) {
                                        viewModel.updateInputText("")
                                        viewModel.clearReplyingTo()

                                        keyboardController?.hide()
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = PhosphorIcons.Bold.PaperPlaneRight,
                                contentDescription = "Gửi",
                                tint = if (inputText.isNotBlank()) Color.White
                                else Color(0xFF7B4FBF).copy(alpha = 0.5f),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CommentItem(
    context: android.content.Context,
    comment: Comment,
    isReply: Boolean = false,
    onReply: () -> Unit
) {
    val user = comment.user
    val avatarLoadError = remember(comment.id) { mutableStateOf(false) }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(if (isReply) 30.dp else 38.dp)
                .clip(CircleShape)
                .background(Color(0xFF7B4FBF).copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            val avatar = user?.avatarUrl
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
                    text = user?.name?.take(1)?.uppercase() ?: "U",
                    color = Color(0xFF7B4FBF),
                    fontSize = if (isReply) 10.sp else 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = user?.name ?: "Người dùng",
                fontSize = if (isReply) 12.sp else 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF111111)
            )

            if (!comment.content.isNullOrEmpty()) {
                Text(
                    text = comment.content,
                    fontSize = if (isReply) 13.sp else 14.sp,
                    color = Color(0xFF333333),
                    lineHeight = 20.sp
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (!comment.createdAt.isNullOrEmpty()) {
                    Text(
                        text = comment.createdAt,
                        fontSize = 11.sp,
                        color = Color(0xFF999999)
                    )
                }

                Row(
                    modifier = Modifier.noRippleClickable(onReply),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = PhosphorIcons.Bold.ArrowBendUpLeft,
                        contentDescription = "Reply",
                        tint = Color(0xFF888888),
                        modifier = Modifier.size(12.dp)
                    )
                    Text(
                        text = "Trả lời",
                        fontSize = 12.sp,
                        color = Color(0xFF888888),
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}