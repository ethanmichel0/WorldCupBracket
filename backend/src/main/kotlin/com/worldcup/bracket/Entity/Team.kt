package com.worldcup.bracket.Entity

import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.annotation.Id

@Document(collection="teams")
class Team (val name: String, val group: String?){
    @Id
    var id: String? = null  // will be same as Id used in football api for simplicity and hence passed in
    var logo: String? = null // used for https://v3.football.api-sports.io

    var winsGroup: Int = 0
    var lossesGroup: Int = 0
    val totalGamesGroup: Int
        get() = winsGroup + lossesGroup + ties
    var goalsForGroup: Int = 0
    var goalsAgainstGroup: Int = 0
    val goalsDifferenceGroup: Int
        get() = goalsForGroup - goalsAgainstGroup
    val pointsGroup: Int
        get() = 3 * winsGroup + ties
    
    var positionGroup: Int  = -1
    // only used to determine first and second place for knockout rounds, -1 indicates not yet calculated

    var winsKnockout: Int = 0
    var lossesKnockout: Int = 0
    val totalGamesKnockout: Int
        get() = winsKnockout + lossesKnockout
    var goalsForKnockout: Int = 0
    var goalsAgainstKnockout: Int = 0
    val goalsDifferenceKnockout: Int
        get() = goalsForKnockout - goalsAgainstKnockout

    val wins: Int
        get() = winsGroup + winsKnockout
    val losses: Int
        get() = lossesGroup + lossesKnockout
    val goalsFor: Int
        get() = goalsForGroup + goalsForKnockout
    val goalsAgainst: Int
        get() = goalsAgainstGroup + goalsAgainstKnockout
    val goalsDifference: Int
        get() = goalsDifferenceGroup + goalsDifferenceKnockout
    var ties: Int = 0 // only can have ties in group stage

    override fun equals(other: Any?): Boolean =
        other is Team && other.name == name
    
    override fun toString(): String = "Team Name:" + name
}