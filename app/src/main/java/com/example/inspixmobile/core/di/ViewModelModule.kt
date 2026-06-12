package com.example.inspixmobile.core.di

import com.example.inspixmobile.presentation.viewmodel.AuthViewModel
import com.example.inspixmobile.presentation.viewmodel.CommentSheetViewModel
import com.example.inspixmobile.presentation.viewmodel.DetailArtistViewModel
import com.example.inspixmobile.presentation.viewmodel.DetailCollectionViewModel
import com.example.inspixmobile.presentation.viewmodel.FollowingViewModel
import com.example.inspixmobile.presentation.viewmodel.HomeViewModel
import com.example.inspixmobile.presentation.viewmodel.ProfileViewModel
import com.example.inspixmobile.presentation.viewmodel.SearchViewModel
import com.example.inspixmobile.presentation.viewmodel.SettingViewModel
import com.example.inspixmobile.presentation.viewmodel.SplashViewModel
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val viewModelModule = module {
    singleOf(::HomeViewModel)
    singleOf(::DetailCollectionViewModel)
    singleOf(::CommentSheetViewModel)
    singleOf(::SearchViewModel)
    singleOf(::AuthViewModel)
    singleOf(::ProfileViewModel)
    singleOf(::SettingViewModel)
    singleOf(::SplashViewModel)
    singleOf(::FollowingViewModel)
    singleOf(::DetailArtistViewModel)
}