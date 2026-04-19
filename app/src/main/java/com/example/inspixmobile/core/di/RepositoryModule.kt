package com.example.inspixmobile.core.di

import com.example.inspixmobile.data.repository.CollectionRepository
import com.example.inspixmobile.domain.contract.repository.ICollectionRepository
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val repositoryModule = module {
    singleOf(::CollectionRepository) bind ICollectionRepository::class
}