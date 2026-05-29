package com.example.inspixmobile.presentation.screen

import androidx.compose.runtime.Composable
import com.composeunstyled.Text
import com.example.inspixmobile.presentation.viewmodel.SearchViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun SearchResultScreen(
    query: String,
    searchViewModel: SearchViewModel = koinViewModel()
) {
    Text(text = "Search result for query: $query")
}