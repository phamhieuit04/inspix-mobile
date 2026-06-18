package com.example.inspixmobile.presentation.screen

import android.content.Context
import android.hardware.SensorManager
import android.net.Uri
import android.util.Log
import android.view.OrientationEventListener
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.view.CameraController
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.regular.CameraRotate
import com.adamglin.phosphoricons.regular.GridFour
import com.adamglin.phosphoricons.regular.ImagesSquare
import com.adamglin.phosphoricons.regular.Lightning
import com.adamglin.phosphoricons.regular.LightningSlash
import com.example.inspixmobile.presentation.state.AspectRatioMode
import com.example.inspixmobile.presentation.viewmodel.UploadViewModel
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel
import java.io.File
import android.graphics.Color as AndroidColor

@Composable
fun UploadCameraScreen(
    bottomContentPadding: Dp = 8.dp,
    navigateToUploadPreview: () -> Unit,
    viewModel: UploadViewModel = koinViewModel()
) {
    val context: Context = LocalContext.current
    val lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()
    val density = LocalDensity.current

    val uiState by viewModel.uiState.collectAsState()

    val cameraController: LifecycleCameraController = remember {
        LifecycleCameraController(context).apply {
            setEnabledUseCases(CameraController.IMAGE_CAPTURE)
        }
    }

    var deviceRotation by remember { mutableFloatStateOf(0f) }
    val shutterAlpha = remember { Animatable(0f) }

    val iconRotation by animateFloatAsState(
        targetValue = deviceRotation,
        animationSpec = spring(dampingRatio = 0.7f, stiffness = 200f),
        label = "iconRotation"
    )

    var headerHeightPx by remember { mutableIntStateOf(0) }
    val headerHeightDp = with(density) { headerHeightPx.toDp() }

    DisposableEffect(Unit) {
        val orientationListener = object : OrientationEventListener(
            context, SensorManager.SENSOR_DELAY_UI
        ) {
            override fun onOrientationChanged(orientation: Int) {
                if (orientation == ORIENTATION_UNKNOWN) return
                deviceRotation = when {
                    orientation in 45..134 -> -90f
                    orientation in 135..224 -> 180f
                    orientation in 225..314 -> 90f
                    else -> 0f
                }
            }
        }
        orientationListener.enable()
        onDispose { orientationListener.disable() }
    }

    LaunchedEffect(uiState.flashEnabled) {
        cameraController.enableTorch(uiState.flashEnabled)
    }

    LaunchedEffect(uiState.isFrontCamera) {
        cameraController.cameraSelector = if (uiState.isFrontCamera) {
            CameraSelector.DEFAULT_FRONT_CAMERA
        } else {
            CameraSelector.DEFAULT_BACK_CAMERA
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        val previewModifier = if (uiState.aspectRatioMode.ratio != null) {
            Modifier
                .align(Alignment.TopCenter)
                .then(
                    when (uiState.aspectRatioMode) {
                        AspectRatioMode.RATIO_3_4 -> Modifier.padding(top = headerHeightDp)
                        AspectRatioMode.RATIO_1_1 -> Modifier.padding(top = headerHeightDp * 2)
                        else -> Modifier
                    }
                )
                .fillMaxWidth()
                .aspectRatio(uiState.aspectRatioMode.ratio!!)
        } else {
            Modifier
                .fillMaxSize()
                .align(Alignment.TopCenter)
        }

        Box(
            modifier = previewModifier
                .statusBarsPadding()
                .clipToBounds()
        ) {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { ctx ->
                    PreviewView(ctx).apply {
                        layoutParams = ViewGroup.LayoutParams(MATCH_PARENT, MATCH_PARENT)
                        setBackgroundColor(AndroidColor.BLACK)
                        implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                        scaleType = PreviewView.ScaleType.FILL_START
                    }.also { previewView ->
                        previewView.controller = cameraController
                        cameraController.bindToLifecycle(lifecycleOwner)
                    }
                }
            )

            if (uiState.showGrid) {
                GridOverlay(modifier = Modifier.fillMaxSize())
            }
        }

        Box(
            modifier = Modifier
                .alpha(shutterAlpha.value)
                .fillMaxSize()
                .background(Color.Black)
        )

        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .statusBarsPadding()
                .onSizeChanged { headerHeightPx = it.height }
                .padding(top = 16.dp, start = 8.dp, end = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { viewModel.toggleFlash() }) {
                    Icon(
                        modifier = Modifier
                            .size(20.dp)
                            .graphicsLayer { rotationZ = iconRotation },
                        imageVector = if (uiState.flashEnabled) PhosphorIcons.Regular.Lightning else PhosphorIcons.Regular.LightningSlash,
                        contentDescription = null,
                        tint = if (uiState.flashEnabled) Color.Yellow else Color.White
                    )
                }

                IconButton(onClick = { viewModel.toggleGrid() }) {
                    Icon(
                        modifier = Modifier
                            .size(22.dp)
                            .graphicsLayer { rotationZ = iconRotation },
                        imageVector = PhosphorIcons.Regular.GridFour,
                        contentDescription = null,
                        tint = if (uiState.showGrid) Color.Yellow else Color.White
                    )
                }

                AspectRatioMode.entries.forEach { mode ->
                    val isSelected = uiState.aspectRatioMode == mode
                    Box(
                        modifier = Modifier
                            .graphicsLayer { rotationZ = iconRotation }
                            .clip(CircleShape)
                            .background(
                                if (isSelected) Color.White.copy(alpha = 0.2f) else Color.Transparent
                            )
                            .clickable { viewModel.setAspectRatio(mode) }
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = mode.label,
                            color = if (isSelected) Color.Yellow else Color.White,
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(bottom = bottomContentPadding)
                .padding(horizontal = 32.dp),
            contentAlignment = Alignment.Center
        ) {
            IconButton(
                modifier = Modifier.align(Alignment.CenterStart),
                onClick = { }
            ) {
                Icon(
                    modifier = Modifier
                        .size(32.dp)
                        .graphicsLayer { rotationZ = iconRotation },
                    imageVector = PhosphorIcons.Regular.ImagesSquare,
                    contentDescription = null,
                    tint = Color.White
                )
            }

            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(80.dp)
                    .border(width = 4.dp, color = Color.White, shape = CircleShape)
                    .clickable(onClick = {
                        scope.launch {
                            shutterAlpha.animateTo(1f, animationSpec = tween(50))
                            shutterAlpha.animateTo(0f, animationSpec = tween(150))
                        }
                        capturePhoto(context, cameraController, uiState.isFrontCamera) { uri ->
                            viewModel.addImage(uri)
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
                modifier = Modifier.align(Alignment.CenterEnd),
                onClick = { viewModel.toggleCamera() }
            ) {
                Icon(
                    modifier = Modifier
                        .size(32.dp)
                        .graphicsLayer { rotationZ = iconRotation },
                    imageVector = PhosphorIcons.Regular.CameraRotate,
                    contentDescription = null,
                    tint = Color.White
                )
            }
        }
    }
}

@Composable
private fun GridOverlay(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.drawWithContent {
            drawContent()
            val strokeColor = Color.White.copy(alpha = 0.4f)
            val strokeWidth = 1.dp.toPx()

            drawLine(
                color = strokeColor,
                start = Offset(size.width / 3f, 0f),
                end = Offset(size.width / 3f, size.height),
                strokeWidth = strokeWidth
            )
            drawLine(
                color = strokeColor,
                start = Offset(size.width * 2f / 3f, 0f),
                end = Offset(size.width * 2f / 3f, size.height),
                strokeWidth = strokeWidth
            )
            drawLine(
                color = strokeColor,
                start = Offset(0f, size.height / 3f),
                end = Offset(size.width, size.height / 3f),
                strokeWidth = strokeWidth
            )
            drawLine(
                color = strokeColor,
                start = Offset(0f, size.height * 2f / 3f),
                end = Offset(size.width, size.height * 2f / 3f),
                strokeWidth = strokeWidth
            )
        }
    )
}

private fun capturePhoto(
    context: Context,
    cameraController: LifecycleCameraController,
    isFrontCamera: Boolean,
    onPhotoCaptured: (Uri) -> Unit
) {
    val mainExecutor = ContextCompat.getMainExecutor(context)
    val file = File(context.cacheDir, "capture_${System.currentTimeMillis()}.jpg")

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