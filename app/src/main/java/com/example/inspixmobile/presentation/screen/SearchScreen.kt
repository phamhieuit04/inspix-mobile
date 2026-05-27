package com.example.inspixmobile.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarColors
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSearchBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Bold
import com.adamglin.phosphoricons.bold.MagnifyingGlass
import com.example.inspixmobile.core.extension.noRippleClickable
import com.example.inspixmobile.presentation.component.TopShadowOverlay
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.materials.CupertinoMaterials
import dev.chrisbanes.haze.materials.ExperimentalHazeMaterialsApi
import dev.chrisbanes.haze.rememberHazeState
import kotlinx.coroutines.launch

@OptIn(ExperimentalHazeMaterialsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(modifier: Modifier = Modifier) {
    val scope = rememberCoroutineScope()

    var query by remember { mutableStateOf("") }
    val searchBarState = rememberSearchBarState()

    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current

    val hazeState = rememberHazeState()
    val hazeStyle = CupertinoMaterials.ultraThin()

    val backgroundColor = Color(0xffe8e7ea)

//    LaunchedEffect(Unit) {
//        focusRequester.requestFocus()
//    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF0F0F5))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .statusBarsPadding(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SearchBar(
                modifier = Modifier
                    .weight(1f)
                    .hazeEffect(hazeState, hazeStyle),
                state = searchBarState,
                inputField = {
                    SearchBarDefaults.InputField(
                        modifier = Modifier.focusRequester(focusRequester),
                        query = query,
                        onQueryChange = { query = it },
                        onSearch = {
                            focusManager.clearFocus()
                            scope.launch { }
                        },
                        expanded = false,
                        onExpandedChange = {},
                        placeholder = {
                            Text(
                                text = "Nhập nội dung...",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 16.sp
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = PhosphorIcons.Bold.MagnifyingGlass,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    )
                }
            )

            Spacer(modifier = Modifier.width(8.dp))

            IconButton(
                onClick = { query = "" },
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = backgroundColor
                )
            ) {
                Icon(
                    Icons.Default.Close,
                    null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        TopShadowOverlay(
            modifier = Modifier.align(Alignment.TopCenter),
            height = 100.dp
        )
    }
}
