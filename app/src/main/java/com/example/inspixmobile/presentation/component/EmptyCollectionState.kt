package com.example.inspixmobile.presentation.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Bold
import com.adamglin.phosphoricons.bold.ImageSquare
import com.composeunstyled.Icon
import com.composeunstyled.Text

@Composable
fun EmptyCollectionState() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 64.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            imageVector = PhosphorIcons.Bold.ImageSquare,
            contentDescription = null,
            modifier = Modifier.size(48.dp),
            tint = Color(0xFFCCCCCC)
        )
        Text(
            text = "Chưa có bộ sưu tập nào",
            fontSize = 15.sp,
            color = Color(0xFFAAAAAA),
            textAlign = TextAlign.Center
        )
    }
}