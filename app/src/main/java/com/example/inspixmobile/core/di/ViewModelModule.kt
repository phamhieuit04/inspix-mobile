package com.example.inspixmobile.core.di

import com.example.inspixmobile.presentation.viewmodel.AuthViewModel
import com.example.inspixmobile.presentation.viewmodel.CommentSheetViewModel
import com.example.inspixmobile.presentation.viewmodel.DetailCollectionViewModel
import com.example.inspixmobile.presentation.viewmodel.HomeViewModel
import com.example.inspixmobile.presentation.viewmodel.SearchViewModel
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val viewModelModule = module {
    singleOf(::HomeViewModel)
    singleOf(::DetailCollectionViewModel)
    singleOf(::CommentSheetViewModel)
    singleOf(::SearchViewModel)
    singleOf(::AuthViewModel)
}