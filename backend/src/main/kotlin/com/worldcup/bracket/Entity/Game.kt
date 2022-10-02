package com.worldcup.bracket.Entity

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import com.worldcup.bracket.Entity.Team

@Document(collection="games")
data class Game(
    var home: Team, 
    var away: Team, 
    var group: String?,
    var knockoutGame: Boolean,
    var date: Long,
    @Id 
    var fixtureId: String,
    var gameNumber: Int? = -1) { // game number used for knockout games
        var homeScore : Int = 0
        var awayScore : Int = 0
        var currentMinute : Int = 0
        var winner: Team? = null
        var scoresAlreadySet: Boolean = false
    }
    