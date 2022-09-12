package com.worldcup.bracket.Entity

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import com.worldcup.bracket.Entity.Team

@Document(collection="games")
data class Game(
    var team1: Team, 
    var team2: Team, 
    var group: Char, 
    var date: Long,
    @Id 
    var fixtureId: String) {
        var homeScore : Int = 0;
        var awayScore : Int = 0;
        var currentMinute : Int = 0;
    }