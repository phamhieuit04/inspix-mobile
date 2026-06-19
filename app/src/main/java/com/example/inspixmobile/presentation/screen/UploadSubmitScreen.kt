package com.example.inspixmobile.presentation.screen

import androidx.activity.compose.BackHandler
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Bold
import com.adamglin.phosphoricons.bold.ArrowDown
import com.adamglin.phosphoricons.bold.ArrowRight
import com.example.inspixmobile.domain.model.Image
import com.example.inspixmobile.domain.model.Topic
import com.example.inspixmobile.presentation.component.BackScaffold
import com.example.inspixmobile.presentation.component.DownloadImageDialog
import com.example.inspixmobile.presentation.component.UploadCollectionDialog
import com.example.inspixmobile.presentation.viewmodel.UploadViewModel
import org.koin.compose.viewmodel.koinViewModel

private val BackgroundColor = Color.White
private val PlaceholderColor = Color.LightGray
private val TextColor = Color.Black
private val LabelColor = Color.DarkGray

@Composable
fun UploadSubmitScreen(
    sharedTransitionScope: SharedTransitionScope,
    bottomContentPadding: Dp = 8.dp,
    navigateToImagesViewer: (List<Image>, Int) -> Unit,
    navigateBack: () -> Unit,
    viewModel: UploadViewModel = koinViewModel()
) {
    val context = LocalContext.current

    val uiState by viewModel.uiState.collectAsState()
    val images = uiState.images

    var title by remember { mutableStateOf<String?>("Tieu de test") }
    var description by remember { mutableStateOf<String?>("Mo ta test") }
    var selectedTopic by remember { mutableStateOf<Topic?>(null) }

    val statusBarPadding = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val backgroundColor = Color(0xFFe8e8e9)
    val iconColor = Color.DarkGray

    val pagerState = rememberPagerState(
        initialPage = 0,
        pageCount = { images.size }
    )

    val topics by viewModel.topics.collectAsStateWithLifecycle()

    val downloadState by viewModel.downloadState.collectAsStateWithLifecycle()
    val uploadState by viewModel.uploadState.collectAsStateWithLifecycle()

    BackHandler { navigateBack() }

    BackScaffold(
        modifier = Modifier.background(Color(0xFFF0F0F5)),
        sharedTransitionScope = sharedTransitionScope,
        onBackPressed = {
            navigateBack()
        }
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF0F0F5)),
            contentAlignment = Alignment.TopCenter
        ) {
            LazyColumn(
                modifier = Modifier
                    .widthIn(max = 600.dp)
                    .fillMaxSize(),
                contentPadding = PaddingValues(
                    top = statusBarPadding,
                    bottom = bottomContentPadding + 32.dp,
                    start = 8.dp,
                    end = 8.dp
                ),
                verticalArrangement = Arrangement.spacedBy(32.dp)
            ) {

                item {
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier
                            .fillMaxSize()
                            .aspectRatio(3f / 4f)
                            .background(color = Color.Black, shape = RoundedCornerShape(12.dp))
                            .clip(RoundedCornerShape(12.dp)),
                    ) { page ->
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Transparent)
                        ) {
                            val image = images[page]
                            val imageKey = requireNotNull(image.uuid)

                            AsyncImage(
                                model = ImageRequest.Builder(context)
                                    .data(image.uri)
                                    .memoryCacheKey(imageKey)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clickable(onClick = {
                                        navigateToImagesViewer(images, page)
                                    })
                            )

                            Box(
                                modifier = Modifier
                                    .align(alignment = Alignment.BottomEnd)
                                    .padding(8.dp)
                                    .shadow(
                                        elevation = 6.dp,
                                        shape = CircleShape,
                                        clip = false
                                    )
                                    .background(
                                        color = backgroundColor,
                                        shape = CircleShape
                                    )
                                    .clip(CircleShape)
                                    .clickable(onClick = {
                                        viewModel.downloadImage(context, image)
                                    })
                                    .padding(12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = PhosphorIcons.Bold.ArrowDown,
                                    contentDescription = "Download",
                                    tint = iconColor,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }

                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(24.dp)
                    ) {

                        UploadTitleField(
                            value = title.orEmpty(),
                            onValueChanged = { title = it },
                            title = "Tiêu đề bộ sưu tập",
                            placeholder = "Nhập tiêu đề cho bộ sưu tập..."
                        )

                        UploadDescriptionField(
                            value = description.orEmpty(),
                            onValueChanged = { description = it },
                            title = "Mô tả bộ sưu tập",
                            placeholder = "Chia sẻ đôi điều về bộ sưu tập này..."
                        )

                        UploadTopicDropdown(
                            topics = topics,
                            selectedTopic = selectedTopic,
                            onTopicSelected = { selectedTopic = it },
                            title = "Chủ đề",
                            placeholder = "Chọn chủ đề cho bộ sưu tập..."
                        )
                    }
                }

                item {
                    Button(
                        onClick = {
                            viewModel.uploadCollection(
                                context = context,
                                title = title,
                                description = description,
                                selectedTopicId = selectedTopic?.id ?: -1,
                                images = images
                            )
                        },
                        modifier = Modifier
                            .padding(horizontal = 12.dp)
                            .fillMaxWidth()
                            .height(64.dp),
                        shape = RoundedCornerShape(80.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AccentPurple),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "Đăng tải",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )

                            Icon(
                                modifier = Modifier.size(20.dp),
                                imageVector = PhosphorIcons.Bold.ArrowRight,
                                contentDescription = "Upload",
                                tint = Color.White,
                            )
                        }
                    }
                }
            }
        }
    }

    DownloadImageDialog(
        state = downloadState,
        onCancel = { viewModel.cancelDownload() },
        onDismiss = { viewModel.dismissDownloadDialog() }
    )

    UploadCollectionDialog(
        state = uploadState,
        onDismiss = { viewModel.dismissUploadDialog() }
    )
}

@Composable
fun UploadTitleField(
    value: String,
    onValueChanged: (String) -> Unit,
    modifier: Modifier = Modifier,
    title: String,
    placeholder: String,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            modifier = Modifier.padding(start = 4.dp),
            text = title.uppercase(),
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 0.8.sp,
            color = LabelColor
        )

        Spacer(modifier = Modifier.height(5.dp))

        OutlinedTextField(
            value = value,
            onValueChange = onValueChanged,
            modifier = Modifier.fillMaxWidth(),
            placeholder = {
                Text(
                    text = placeholder,
                    fontSize = 15.sp,
                    lineHeight = 15.sp,
                    color = PlaceholderColor
                )
            },
            textStyle = LocalTextStyle.current.copy(
                fontSize = 15.sp,
                lineHeight = 15.sp,
                color = TextColor
            ),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            shape = RoundedCornerShape(36.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = BackgroundColor,
                unfocusedContainerColor = BackgroundColor,
                focusedBorderColor = AccentPurple,
                unfocusedBorderColor = Color(0xFFD3D1C7),
                focusedTextColor = TextColor,
                unfocusedTextColor = TextColor
            )
        )
    }
}

@Composable
fun UploadDescriptionField(
    value: String,
    onValueChanged: (String) -> Unit,
    modifier: Modifier = Modifier,
    title: String,
    placeholder: String,
    minLines: Int = 5
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            modifier = Modifier.padding(start = 4.dp),
            text = title.uppercase(),
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 0.8.sp,
            color = LabelColor
        )

        Spacer(modifier = Modifier.height(5.dp))

        OutlinedTextField(
            value = value,
            onValueChange = onValueChanged,
            modifier = Modifier.fillMaxWidth(),
            placeholder = {
                Text(
                    text = placeholder,
                    fontSize = 15.sp,
                    lineHeight = 22.sp,
                    color = PlaceholderColor
                )
            },
            textStyle = LocalTextStyle.current.copy(
                fontSize = 15.sp,
                lineHeight = 22.sp,
                color = TextColor
            ),
            singleLine = false,
            minLines = minLines,
            shape = RoundedCornerShape(20.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = BackgroundColor,
                unfocusedContainerColor = BackgroundColor,
                focusedBorderColor = AccentPurple,
                unfocusedBorderColor = Color(0xFFD3D1C7),
                focusedTextColor = TextColor,
                unfocusedTextColor = TextColor
            )
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UploadTopicDropdown(
    topics: List<Topic>,
    selectedTopic: Topic?,
    onTopicSelected: (Topic) -> Unit,
    modifier: Modifier = Modifier,
    title: String,
    placeholder: String
) {
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            modifier = Modifier.padding(start = 4.dp),
            text = title.uppercase(),
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 0.8.sp,
            color = LabelColor
        )

        Spacer(modifier = Modifier.height(5.dp))

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = it }
        ) {
            OutlinedTextField(
                value = selectedTopic?.name.orEmpty(),
                onValueChange = {},
                readOnly = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(MenuAnchorType.PrimaryNotEditable),
                placeholder = {
                    Text(
                        text = placeholder,
                        fontSize = 15.sp,
                        lineHeight = 15.sp,
                        color = PlaceholderColor
                    )
                },
                textStyle = androidx.compose.ui.text.TextStyle(
                    fontSize = 15.sp,
                    lineHeight = 15.sp,
                    color = TextColor
                ),
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                },
                singleLine = true,
                shape = RoundedCornerShape(36.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = BackgroundColor,
                    unfocusedContainerColor = BackgroundColor,
                    focusedBorderColor = AccentPurple,
                    unfocusedBorderColor = Color(0xFFD3D1C7),
                    focusedTextColor = TextColor,
                    unfocusedTextColor = TextColor,
                    focusedTrailingIconColor = AccentPurple,
                    unfocusedTrailingIconColor = LabelColor
                )
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                shape = RoundedCornerShape(16.dp),
                containerColor = Color.White
            ) {
                topics.forEach { topic ->
                    val isSelected = topic.id == selectedTopic?.id
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = topic.name.orEmpty(),
                                fontSize = 15.sp,
                                color = if (isSelected) AccentPurple else TextColor,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                            )
                        },
                        onClick = {
                            onTopicSelected(topic)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}