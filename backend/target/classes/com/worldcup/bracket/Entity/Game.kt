package com.worldcup.bracket.Entity

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import com.worldcup.bracket.Entity.Team

import java.util.Date

@Document(collection="games")
data class Game(
    var team1: Team, 
    var team2: Team, 
    var knockout: Boolean, 
    var date: Date,
    @Id 
    var fixtureId: String)