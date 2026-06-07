package com.example.inspixmobile.core.di

import com.example.inspixmobile.data.source.local.store.SessionStore
import com.example.inspixmobile.data.source.local.store.SettingStore
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val storeModule = module {
    single { SessionStore(androidContext()) }
    single { SettingStore(androidContext()) }
}