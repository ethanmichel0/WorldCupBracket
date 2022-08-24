package com.worldcup.bracket.Entity


import org.bson.types.ObjectId
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document(collection="games")
class Game(home: Team, away: Team) {
    @Id
    val id: String? = ObjectId().toHexString()
    val home: Team = home
    val away: Team = away
}