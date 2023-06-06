package com.worldcup.bracket.Entity

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.mapping.DocumentReference
import org.springframework.data.annotation.ReadOnlyProperty

import com.fasterxml.jackson.annotation.JsonIgnore
import org.bson.types.ObjectId

@Document(collection="users")
data class User(
    val principalId: String,
    val name: String,
    val email: String, 
    val service: AuthService, 
    @JsonIgnore
    var password: String? = null,
    val createdDate: Long = System.currentTimeMillis() / 1000,
    val draftGroups: MutableList<ObjectId> = mutableListOf<ObjectId>(),
    @Id 
    val id : ObjectId = ObjectId.get(),
    ) {
        override fun equals(other: Any?): Boolean =
            other is User && other.name == name && other.email == email
        var admin : Boolean = false
    }

enum class AuthService {
    GOOGLE
}