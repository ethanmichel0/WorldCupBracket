package com.worldcup.bracket.Entity

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

import org.bson.types.ObjectId


@Document(collection="draftgroups")
data class DraftGroups(
    @Id 
    val id : ObjectId = ObjectId.get(),
    val name: String, 
    var owner: User, 
    val createdDate: Long = System.currentTimeMillis() / 1000,
    val password: String,
    var draftTime: Long = -1,
    val members: List<User> = mutableListOf<User>()
)
    