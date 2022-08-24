package com.worldcup.bracket.Entity

import org.bson.types.ObjectId
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document(collection="groups")
class Group(letter: Char, teams: List<Team>) {
    @Id
    val id: String? = ObjectId().toHexString()
    val letter: Char = letter
    val teams: List<Team> = teams
    val games: List<Game>? = null
    val first: Team? = null
    val second: Team? = null
}