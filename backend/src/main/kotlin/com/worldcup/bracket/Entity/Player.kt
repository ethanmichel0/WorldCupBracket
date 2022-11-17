package com.worldcup.bracket.Entity

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import com.worldcup.bracket.Entity.Team

@Document(collection="players")
data class Player(
    val team: Team,
    val position: Position,
    @Id 
    var id: String,
    val name: String,
    val age: Int,
    val height: String
    ) { // game number used for knockout games
        var goals : Int = 0
        var assists : Int = 0
        var totalPoints: Int = 0
    }

enum class Position {
    Goalkeeper,
    Defender,
    Midfielder,
    Attacker
}