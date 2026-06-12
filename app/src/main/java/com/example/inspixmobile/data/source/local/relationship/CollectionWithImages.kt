package com.example.inspixmobile.data.source.local.relationship

import androidx.room.Embedded
import androidx.room.Relation
import com.example.inspixmobile.data.source.local.entity.CollectionEntity
import com.example.inspixmobile.data.source.local.entity.ImageEntity

data class CollectionWithImages(
    @Embedded val collection: CollectionEntity,
    @Relation(
        parentColumn = "uuid",
        entityColumn = "collection_uuid"
    )
    val images: List<ImageEntity>
)