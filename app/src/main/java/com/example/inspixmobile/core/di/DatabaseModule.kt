package com.example.inspixmobile.core.di

import androidx.room.Room
import com.example.inspixmobile.data.source.local.db.AppDatabase
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val databaseModule = module {
    single<AppDatabase> {
        Room.databaseBuilder(
            androidContext(),
            AppDatabase::class.java,
            "inspix.db"
        ).fallbackToDestructiveMigration(true)
            .build()
    }

    single { get<AppDatabase>().collectionDao() }
    single { get<AppDatabase>().imageDao() }
    single { get<AppDatabase>().remoteKeyDao() }
}

