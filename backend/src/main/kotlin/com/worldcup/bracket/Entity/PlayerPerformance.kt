package com.worldcup.bracket.Entity

import org.bson.types.ObjectId

abstract class PlayerPerformance {
    abstract val playerSeason: PlayerSeason
    abstract val game: Game
    abstract var minutes: Int?
    abstract val id : ObjectId
}
    