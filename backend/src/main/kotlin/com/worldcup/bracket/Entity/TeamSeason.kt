package com.worldcup.bracket.Entity

import org.springframework.data.annotation.Id
import org.bson.types.ObjectId

import org.springframework.data.mongodb.core.mapping.Document
import com.worldcup.bracket.Entity.Team

@Document(collection="teamseasons")
data class TeamSeason(
    val team: Team,
    val season: Int,
    val league: League,
    var position: Int = -1,
    @Id 
    val id: ObjectId = ObjectId.get()
) {

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
    var ties: Int = 0

    var current = true
}