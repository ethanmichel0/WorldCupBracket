package com.worldcup.bracket.Entity

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import com.fasterxml.jackson.annotation.JsonIgnore

import org.bson.types.ObjectId


@Document(collection="draftgroups")
data class DraftGroup(
    @Id 
    val id : ObjectId = ObjectId.get(),
    val name: String, 
    var owner: User, 
    val createdDate: Long = System.currentTimeMillis() / 1000,
    @JsonIgnore
    val password: String,
    @JsonIgnore
    var draftTime: Long = -1,
    var members: MutableList<User> = mutableListOf<User>(),
    val availablePlayers: MutableList<Player> = mutableListOf<Player>()
)
    