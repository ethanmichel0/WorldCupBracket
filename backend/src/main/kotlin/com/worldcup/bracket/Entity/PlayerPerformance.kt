package com.worldcup.bracket.Entity

import org.springframework.data.mongodb.core.mapping.Document
import org.bson.types.ObjectId

abstract class PlayerPerformance {
    abstract val player: Player
    abstract val game: Game
    abstract var minutes: Int?
    abstract val id : ObjectId
}
    