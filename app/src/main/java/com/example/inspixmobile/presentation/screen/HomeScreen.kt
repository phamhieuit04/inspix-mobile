package com.example.inspixmobile.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.example.inspixmobile.domain.model.Collection
import com.example.inspixmobile.domain.model.Image

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen() {
    val collections = remember { fakeCollections() }
    var searchQuery by remember { mutableStateOf("") }
    val topics = listOf("All", "Nature", "Architecture", "Minimal", "Abstract", "People")
    var selectedTopic by remember { mutableStateOf("All") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
    ) {

        LazyVerticalStaggeredGrid(
            columns = StaggeredGridCells.Fixed(2),
            contentPadding = PaddingValues(top = 160.dp, start = 8.dp, end = 8.dp, bottom = 120.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalItemSpacing = 8.dp,
            modifier = Modifier.fillMaxSize()
        ) {
            items(collections) { collection ->
                CollectionCard(collection = collection)
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.White.copy(alpha = 0.95f), Color.Transparent),
                        startY = 0f,
                        endY = 400f
                    )
                )
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Explore curated art...", color = Color(0xFFAAAAAA)) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = Color(0xFFAAAAAA)
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(50),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = Color(0xFFE0E0E0),
                    focusedBorderColor = Color(0xFF9C6FD6),
                    unfocusedContainerColor = Color.White,
                    focusedContainerColor = Color.White
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(topics) { topic ->
                    val isSelected = topic == selectedTopic
                    Surface(
                        onClick = { selectedTopic = topic },
                        shape = RoundedCornerShape(50),
                        color = if (isSelected) Color(0xFF7B4FBF) else Color(0xFFF0F0F0)
                    ) {
                        Text(
                            text = topic,
                            color = if (isSelected) Color.White else Color(0xFF444444),
                            fontSize = 13.sp,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CollectionCard(collection: Collection) {
    var isLiked by remember { mutableStateOf(collection.isLiked ?: false) }
    val thumbnailUrl = collection.images?.firstOrNull()?.urlRegular

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
    ) {
        AsyncImage(
            model = thumbnailUrl,
            contentDescription = collection.title,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
        )

        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(8.dp)
                .size(36.dp)
                .background(Color.White.copy(alpha = 0.85f), CircleShape)
                .clickable { isLiked = !isLiked },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                contentDescription = null,
                tint = if (isLiked) Color(0xFFE53935) else Color(0xFF666666),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

fun fakeCollections(): List<Collection> {
    val imageUrls = listOf(
        "https://picsum.photos/seed/forest/400/600",
        "https://picsum.photos/seed/arch/400/300",
        "https://picsum.photos/seed/mountain/400/500",
        "https://picsum.photos/seed/city/400/400",
        "https://picsum.photos/seed/ocean/400/550",
        "https://picsum.photos/seed/desert/400/350",
        "https://picsum.photos/seed/night/400/480",
        "https://picsum.photos/seed/valley/400/420",
        "https://picsum.photos/seed/abstract/400/360",
        "https://picsum.photos/seed/portrait/400/500",
        "https://picsum.photos/seed/minimal/400/300",
        "https://picsum.photos/seed/sky/400/440",
        "https://picsum.photos/seed/golden/400/380",
        "https://picsum.photos/seed/blue/400/520",
        "https://picsum.photos/seed/stone/400/340",
        "https://picsum.photos/seed/bloom/400/460",
        "https://picsum.photos/seed/shadow/400/400",
        "https://picsum.photos/seed/lake/400/540",
        "https://picsum.photos/seed/texture/400/320",
        "https://picsum.photos/seed/light/400/480"
    )

    val titles = listOf(
        "Misty Mountains", "Forest Paths", "Urban Lines", "Wild Colors",
        "Ocean Calm", "Desert Light", "Night City", "Green Valley",
        "Abstract Flow", "Portrait Series", "Minimal Space", "Sky High",
        "Golden Hour", "Deep Blue", "Stone & Steel", "Bloom Season",
        "Shadow Play", "Mirror Lake", "Texture World", "Soft Light"
    )

    val topics = listOf("Nature", "Architecture", "Minimal", "Abstract", "People")

    return List(20) { index ->
        Collection(
            id = index.toLong() + 1,
            userId = 1L,
            topicId = index % 5,
            title = titles[index],
            description = "A curated collection of ${titles[index].lowercase()} imagery.",
            totalLikes = (10..999).random(),
            isLiked = index % 4 == 0,
            topicName = topics[index % 5],
            images = listOf(
                Image(
                    uuid = "img-$index",
                    urlSmall = imageUrls[index],
                    urlRegular = imageUrls[index],
                    urlFull = imageUrls[index]
                )
            )
        )
    }
}