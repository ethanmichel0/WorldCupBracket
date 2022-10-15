package com.worldcup.bracket.Entity

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

import org.bson.types.ObjectId


@Document(collection="bracket")
data class Bracket(
    @Id 
    val id : ObjectId = ObjectId.get(),
    var points: Int = 0, 
    var user: User, 
    val createdDate: Long = System.currentTimeMillis() / 1000
)
    