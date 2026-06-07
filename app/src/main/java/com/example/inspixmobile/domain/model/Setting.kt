package com.example.inspixmobile.domain.model

import com.example.inspixmobile.presentation.component.NavigationBarStyle
import com.example.inspixmobile.presentation.screen.HomeLayoutStyle

data class Setting(
    val homeLayout: HomeLayoutStyle? = null,
    val navbarLayout: NavigationBarStyle? = null
)