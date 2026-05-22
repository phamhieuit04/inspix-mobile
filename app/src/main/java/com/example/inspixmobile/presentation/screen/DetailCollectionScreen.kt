package com.example.inspixmobile.presentation.screen

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.regular.ArrowLeft
import com.composeunstyled.Text
import com.example.inspixmobile.core.extension.noRippleClickable

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailCollectionScreen(
    uuid: String,
    navigateBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detail Collection") },
                navigationIcon = {
                    IconButton(onClick = navigateBack) {
                        Icon(
                            imageVector = PhosphorIcons.Regular.ArrowLeft,
                            contentDescription = "Back",
                            modifier = Modifier.noRippleClickable { navigateBack() }
                        )
                    }
                }
            )
        }
    ) { padding ->
        Text(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp),
            text = "UUID: $uuid"
        )
    }
}