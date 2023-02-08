package com.worldcup.bracket.Entity

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import com.worldcup.bracket.Entity.Team

@Document(collection="games")
data class Game(
    var home: TeamSeason, 
    var away: TeamSeason, 
    var knockoutGame: Boolean,
    var date: Long,
    @Id 
    var fixtureId: String,
    var league: League) {
        var homeScore : Int = 0
        var awayScore : Int = 0
        var currentMinute : Int = 0
        var winner: TeamSeason? = null
        var scoresAlreadySet: Boolean = false

        override fun equals(other: Any?): Boolean =
            other is Game && other.home.team == home.team && other.away.team == away.team && date == other.date
    }
    