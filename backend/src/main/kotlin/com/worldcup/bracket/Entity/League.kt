package com.worldcup.bracket.Entity

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

import org.bson.types.ObjectId


@Document(collection="leagues")
data class League(
    @Id 
    val id: String,
    val name: String,
    val country: String,
    val sport: Sport,
    val playoffs: Boolean,
    val logo: String
)

enum class Sport {
    Soccer
}
    