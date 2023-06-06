package com.worldcup.bracket.Entity

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

import org.bson.types.ObjectId


@Document(collection="playerdrafts")
data class PlayerDraft(
    val userEmail: String,
    val draftGroup: DraftGroup,
    @Id 
    val id: ObjectId = ObjectId.get(),
    val players: MutableList<PlayerSeason> = mutableListOf<PlayerSeason>(),
    val watchList: MutableList<PlayerSeason> = mutableListOf<PlayerSeason>(),
    var points: Int = 0
)
    