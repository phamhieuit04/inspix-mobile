package com.example.inspixmobile.core.di

import com.example.inspixmobile.data.repository.AuthRepository
import com.example.inspixmobile.data.repository.CollectionInteractionRepository
import com.example.inspixmobile.data.repository.CollectionRepository
import com.example.inspixmobile.data.repository.CommentRepository
import com.example.inspixmobile.data.repository.ImageRepository
import com.example.inspixmobile.data.repository.TopicRepository
import com.example.inspixmobile.data.repository.UserInteractionRepository
import com.example.inspixmobile.data.repository.UserRepository
import com.example.inspixmobile.domain.contract.repository.IAuthRepository
import com.example.inspixmobile.domain.contract.repository.ICollectionInteractionRepository
import com.example.inspixmobile.domain.contract.repository.ICollectionRepository
import com.example.inspixmobile.domain.contract.repository.ICommentRepository
import com.example.inspixmobile.domain.contract.repository.IImageRepository
import com.example.inspixmobile.domain.contract.repository.ITopicRepository
import com.example.inspixmobile.domain.contract.repository.IUserInteractionRepository
import com.example.inspixmobile.domain.contract.repository.IUserRepository
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val repositoryModule = module {
    singleOf(::CollectionRepository) bind ICollectionRepository::class
    singleOf(::TopicRepository) bind ITopicRepository::class
    singleOf(::CommentRepository) bind ICommentRepository::class
    singleOf(::AuthRepository) bind IAuthRepository::class
    singleOf(::UserRepository) bind IUserRepository::class
    singleOf(::CollectionInteractionRepository) bind ICollectionInteractionRepository::class
    singleOf(::UserInteractionRepository) bind IUserInteractionRepository::class
    singleOf(::ImageRepository) bind IImageRepository::class
}