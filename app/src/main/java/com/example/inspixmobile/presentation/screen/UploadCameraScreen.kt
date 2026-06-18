package com.example.inspixmobile.presentation.screen

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.LinearLayout
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.view.CameraController
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.regular.CameraRotate
import com.adamglin.phosphoricons.regular.ImagesSquare
import com.example.inspixmobile.core.extension.rotateBitmap
import com.example.inspixmobile.core.extension.saveToCache
import com.example.inspixmobile.presentation.viewmodel.UploadViewModel
import org.koin.compose.viewmodel.koinViewModel
import java.io.File
import java.util.concurrent.Executor
import android.graphics.Color as AndroidColor

@Composable
fun UploadCameraScreen(
    bottomContentPadding: Dp = 8.dp,
    navigateToUploadPreview: () -> Unit,
    viewModel: UploadViewModel = koinViewModel()
) {
    val context: Context = LocalContext.current
    val lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current
    val cameraController: LifecycleCameraController =
        remember {
            LifecycleCameraController(context).apply {
                setEnabledUseCases(
                    CameraController.IMAGE_CAPTURE
                )
            }
        }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        AndroidView(
            factory = { context ->
                PreviewView(context).apply {
                    layoutParams = LinearLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT)
                    setBackgroundColor(AndroidColor.BLACK)
                    implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                    scaleType = PreviewView.ScaleType.FIT_CENTER
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
                    )
                    .clickable(onClick = {
                        capturePhoto(context, cameraController) { bitmap ->
                            viewModel.addImage(bitmap)
                            navigateToUploadPreview()
                        }
                    }),
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

private fun capturePhoto(
    context: Context,
    cameraController: LifecycleCameraController,
    onPhotoCaptured: (Uri) -> Unit
) {
    val mainExecutor = ContextCompat.getMainExecutor(context)

    val file = File(
        context.cacheDir,
        "capture_${System.currentTimeMillis()}.jpg"
    )

    val isFrontCamera = cameraController.cameraSelector == CameraSelector.DEFAULT_FRONT_CAMERA

    val outputOptions = ImageCapture.OutputFileOptions.Builder(file)
        .setMetadata(
            ImageCapture.Metadata().apply {
                isReversedHorizontal = isFrontCamera
            }
        )
        .build()

    cameraController.takePicture(
        outputOptions,
        mainExecutor,
        object : ImageCapture.OnImageSavedCallback {
            override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                onPhotoCaptured(file.toUri())
            }

            override fun onError(exception: ImageCaptureException) {
                Log.e("myapp", "Error capturing image", exception)
            }
        }
    )
}