package com.worldcup.bracket.Entity

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import org.bson.types.ObjectId

@Document(collection="playerperformances")
data class PlayerPerformance(
    val player: Player,
    val game: Game,
    var minutes: Int?,
    val started: Boolean, // this value is always given from repo as true or false
    var goals: Int? = 0,
    var assists: Int? = 0,
    var yellowCards: Int? = 0,
    var redCards: Int? = 0,
    var cleanSheet: Boolean, // manually calculated in game service, so no need to be nullable
    var saves: Int? = 0,
    var penaltySaves: Int? = 0,
    var penaltyMisses: Int? = 0,
    var ownGoals: Int = 0,
    @Id 
    val id : ObjectId = ObjectId.get(),
)
    