package com.example.inspixmobile.presentation.screen

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.LinearLayout
import androidx.activity.compose.BackHandler
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Bold
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.bold.CameraRotate
import com.adamglin.phosphoricons.regular.CameraRotate
import com.adamglin.phosphoricons.regular.Images
import com.adamglin.phosphoricons.regular.ImagesSquare
import com.example.inspixmobile.core.extension.rotateBitmap
import com.example.inspixmobile.presentation.component.BackScaffold
import java.util.concurrent.Executor
import android.graphics.Color as AndroidColor

@Composable
fun UploadScreen(
    bottomContentPadding: Dp = 8.dp
) {
    val context: Context = LocalContext.current
    val lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current
    val cameraController: LifecycleCameraController =
        remember { LifecycleCameraController(context) }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        AndroidView(
            factory = { context ->
                PreviewView(context).apply {
                    layoutParams = LinearLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT)
                    setBackgroundColor(AndroidColor.BLACK)
                    implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                    scaleType = PreviewView.ScaleType.FILL_START
                }.also { previewView ->
                    previewView.controller = cameraController
                    cameraController.bindToLifecycle(lifecycleOwner)
                }
            }
        )

        Box(
            modifier = Modifier
                .align(alignment = Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(bottom = bottomContentPadding)
                .padding(horizontal = 32.dp),
            contentAlignment = Alignment.Center
        ) {
            IconButton(
                modifier = Modifier
                    .align(alignment = Alignment.CenterStart),
                onClick = { }
            ) {
                Icon(
                    modifier = Modifier.size(32.dp),
                    imageVector = PhosphorIcons.Regular.ImagesSquare,
                    contentDescription = null,
                    tint = Color.White
                )
            }

            Box(
                modifier = Modifier
                    .align(alignment = Alignment.Center)
                    .size(80.dp)
                    .border(
                        width = 4.dp,
                        color = Color.White,
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .background(Color.White, CircleShape)
                )
            }

            IconButton(
                modifier = Modifier
                    .align(alignment = Alignment.CenterEnd),
                onClick = {
                    cameraController.cameraSelector =
                        if (cameraController.cameraSelector == CameraSelector.DEFAULT_BACK_CAMERA) {
                            CameraSelector.DEFAULT_FRONT_CAMERA
                        } else CameraSelector.DEFAULT_BACK_CAMERA
                }
            ) {
                Icon(
                    modifier = Modifier.size(32.dp),
                    imageVector = PhosphorIcons.Regular.CameraRotate,
                    contentDescription = null,
                    tint = Color.White
                )
            }
        }
    }
}