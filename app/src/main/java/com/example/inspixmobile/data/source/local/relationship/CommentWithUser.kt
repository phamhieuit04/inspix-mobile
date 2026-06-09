package com.example.inspixmobile.data.source.local.relationship

import androidx.room.Embedded
import androidx.room.Relation
import com.example.inspixmobile.data.source.local.entity.CommentEntity
import com.example.inspixmobile.data.source.local.entity.UserEntity

data class CommentWithUser(
    @Embedded val comment: CommentEntity,
    @Relation(
        parentColumn = "user_uuid",
        entityColumn = "uuid"
    )
    val user: UserEntity?
)