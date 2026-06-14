package com.example.inspixmobile.presentation.component

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Bold
import com.adamglin.phosphoricons.bold.CheckCircle
import com.adamglin.phosphoricons.bold.DownloadSimple
import com.adamglin.phosphoricons.bold.WarningCircle
import com.example.inspixmobile.presentation.screen.AccentPurple

sealed interface DownloadState {
    object Idle : DownloadState
    data class Downloading(val progress: Float) : DownloadState
    object Done : DownloadState
    object Error : DownloadState
}

@Composable
fun DownloadImageDialog(
    state: DownloadState,
    onCancel: () -> Unit,
    onDismiss: () -> Unit
) {
    if (state is DownloadState.Idle) return

    val isTerminal = state is DownloadState.Done || state is DownloadState.Error

    Dialog(
        onDismissRequest = { if (isTerminal) onDismiss() },
        properties = DialogProperties(
            dismissOnBackPress = isTerminal,
            dismissOnClickOutside = isTerminal
        )
    ) {
        Card(
            shape = RoundedCornerShape(28.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .padding(top = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                AnimatedContent(
                    targetState = state,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "download_icon"
                ) { currentState ->
                    val (bgColor, tintColor, icon) = when (currentState) {
                        is DownloadState.Done -> Triple(
                            Color(0xFFEEF2FF), AccentPurple, PhosphorIcons.Bold.CheckCircle
                        )

                        is DownloadState.Error -> Triple(
                            Color(0xFFFFF1F2), Color(0xFFE11D48), PhosphorIcons.Bold.WarningCircle
                        )

                        else -> Triple(
                            Color(0xFFEEF2FF), AccentPurple, PhosphorIcons.Bold.DownloadSimple
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(bgColor),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            modifier = Modifier.size(40.dp),
                            tint = tintColor
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                AnimatedContent(
                    targetState = state,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "download_title"
                ) { currentState ->
                    Text(
                        text = when (currentState) {
                            is DownloadState.Done -> "Tải xuống hoàn tất!"
                            is DownloadState.Error -> "Tải xuống thất bại!"
                            else -> "Đang tải xuống..."
                        },
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                AnimatedContent(
                    targetState = state,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "download_subtitle"
                ) { currentState ->
                    Text(
                        text = when (currentState) {
                            is DownloadState.Done ->
                                "Ảnh đã được lưu vào thư mục\nPictures/Inspix."

                            is DownloadState.Error ->
                                "Đã xảy ra lỗi trong quá trình tải.\nVui lòng thử lại sau nha."

                            else ->
                                "Vui lòng đợi trong giây lát,\nđừng tắt ứng dụng nha."
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 20.sp
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                AnimatedContent(
                    targetState = state,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "download_progress"
                ) { currentState ->
                    if (currentState is DownloadState.Downloading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(32.dp),
                            color = AccentPurple,
                            strokeWidth = 3.dp,
                            strokeCap = StrokeCap.Round
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = { if (isTerminal) onDismiss() else onCancel() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp),
                    shape = RoundedCornerShape(64.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = when (state) {
                            is DownloadState.Done -> AccentPurple
                            is DownloadState.Error -> Color(0xFFE11D48)
                            else -> Color(0xFFF1F5F9)
                        }
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                ) {
                    Text(
                        text = when (state) {
                            is DownloadState.Done -> "Hoàn tất"
                            is DownloadState.Error -> "Đóng"
                            else -> "Hủy"
                        },
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = when (state) {
                            is DownloadState.Downloading -> Color(0xFF64748B)
                            else -> Color.White
                        }
                    )
                }
            }
        }
    }
}