package com.worldcup.bracket.Entity

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

import org.bson.types.ObjectId

@Document(collection="users")
data class User(
    @Id 
    val id : ObjectId = ObjectId.get(),
    var email: String, 
    var service: String, 
    var password: String?,
    val createdDate: Long = System.currentTimeMillis() / 1000)
    