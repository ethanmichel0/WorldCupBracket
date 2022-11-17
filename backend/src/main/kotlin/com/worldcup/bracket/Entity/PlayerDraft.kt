package com.worldcup.bracket.Entity

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

import org.bson.types.ObjectId


@Document(collection="playerdrafts")
data class PlayerDraft(
    @Id 
    val id: ObjectId = ObjectId.get(),
    val players: MutableList<Player> = mutableListOf<Player>(),
    var points: Int = 0, 
    val user: User,
    val draftGroup: DraftGroup
)
    