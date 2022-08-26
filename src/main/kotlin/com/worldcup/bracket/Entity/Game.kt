package com.worldcup.bracket.Entity

import org.bson.types.ObjectId
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import com.worldcup.bracket.Entity.Team

@Document(collection="games")
class Game(team1: Team, team2: Team, knockout: Boolean) {
    @Id
    var id: String? = ObjectId().toHexString()
    var team1: Team = team1
    var team2: Team = team2
    var knockout: Boolean = knockout
}