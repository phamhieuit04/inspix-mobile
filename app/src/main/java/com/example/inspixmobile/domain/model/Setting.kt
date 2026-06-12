package com.example.inspixmobile.domain.model

import com.example.inspixmobile.presentation.component.NavigationBarStyle
import com.example.inspixmobile.presentation.screen.HomeLayoutStyle

data class Setting(
    var homeLayout: HomeLayoutStyle = HomeLayoutStyle.Grid,
    var navbarLayout: NavigationBarStyle = NavigationBarStyle.Floating
)