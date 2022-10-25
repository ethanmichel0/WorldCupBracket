package com.worldcup.bracket.Entity

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

import org.bson.types.ObjectId

@Document(collection="users")
data class User(
    val name: String,
    val email: String, 
    val service: AuthService, 
    var password: String? = null,
    val createdDate: Long = System.currentTimeMillis() / 1000,
    @Id 
    val id : ObjectId = ObjectId.get())

enum class AuthService {
    GOOGLE
}