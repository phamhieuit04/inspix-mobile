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
import androidx.compose.material3.LinearProgressIndicator
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
import com.example.inspixmobile.presentation.screen.AccentPurple

sealed interface DownloadState {
    object Idle : DownloadState
    data class Downloading(val progress: Float) : DownloadState
    object Done : DownloadState
}

@Composable
fun DownloadImageDialog(
    state: DownloadState,
    onCancel: () -> Unit,
    onDismiss: () -> Unit
) {
    if (state is DownloadState.Idle) return

    val isDone = state is DownloadState.Done

    Dialog(
        onDismissRequest = { if (isDone) onDismiss() },
        properties = DialogProperties(dismissOnBackPress = isDone, dismissOnClickOutside = isDone)
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
                    targetState = isDone,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "download_icon"
                ) { done ->
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFEEF2FF)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (done) {
                            Icon(
                                imageVector = PhosphorIcons.Bold.CheckCircle,
                                contentDescription = null,
                                modifier = Modifier.size(40.dp),
                                tint = AccentPurple
                            )
                        } else {
                            Icon(
                                imageVector = PhosphorIcons.Bold.DownloadSimple,
                                contentDescription = null,
                                modifier = Modifier.size(40.dp),
                                tint = AccentPurple
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                AnimatedContent(
                    targetState = isDone,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "download_title"
                ) { done ->
                    Text(
                        text = if (done) "Tải xuống hoàn tất!" else "Đang tải xuống...",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                AnimatedContent(
                    targetState = isDone,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "download_subtitle"
                ) { done ->
                    Text(
                        text = if (done)
                            "Ảnh đã được lưu vào thư mục\nPictures/Inspix."
                        else
                            "Vui lòng đợi trong giây lát,\nđừng tắt ứng dụng nha.",
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
                    onClick = { if (isDone) onDismiss() else onCancel() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp),
                    shape = RoundedCornerShape(64.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isDone) AccentPurple else Color(0xFFF1F5F9)
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                ) {
                    Text(
                        text = if (isDone) "Hoàn tất" else "Hủy",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isDone) Color.White else Color(0xFF64748B)
                    )
                }
            }
        }
    }
}