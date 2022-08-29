package com.worldcup.bracket.Entity

import org.bson.types.ObjectId
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import com.worldcup.bracket.Entity.Team
import com.worldcup.bracket.Entity.Game

@Document(collection="groups")
class Group(var letter: Char, var teams: List<Team>) {
    @Id
    var id: String? = ObjectId().toHexString()
    var first: Team? = null
    var second: Team? = null
    var games: List<Game>? = null
}