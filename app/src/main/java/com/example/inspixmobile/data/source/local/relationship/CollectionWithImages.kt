package com.example.inspixmobile.data.source.local.relationship

import androidx.room.Embedded
import androidx.room.Relation
import com.example.inspixmobile.data.source.local.entity.CollectionEntity
import com.example.inspixmobile.data.source.local.entity.ImageEntity

data class CollectionWithImages(
    @Embedded val collection: CollectionEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "collection_id"
    )
    val images: List<ImageEntity>
)


