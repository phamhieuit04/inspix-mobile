package com.example.inspixmobile.data.source.local.relationship

import androidx.room.Embedded
import androidx.room.Relation
import com.example.inspixmobile.data.source.local.entity.CollectionEntity
import com.example.inspixmobile.data.source.local.entity.ImageEntity
import com.example.inspixmobile.data.source.local.entity.UserEntity

data class CollectionWithImagesAndAuthor(
    @Embedded val collection: CollectionEntity,
    @Relation(
        parentColumn = "uuid",
        entityColumn = "collection_uuid"
    )
    val images: List<ImageEntity>,
    @Relation(
        parentColumn = "user_uuid",
        entityColumn = "uuid"
    )
    val author: UserEntity?
)



