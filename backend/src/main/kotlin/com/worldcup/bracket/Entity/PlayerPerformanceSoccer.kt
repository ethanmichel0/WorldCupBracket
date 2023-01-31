package com.worldcup.bracket.Entity
import org.springframework.data.mongodb.core.mapping.Document
import org.bson.types.ObjectId
import org.springframework.data.annotation.Id

@Document(collection="playerperformancessoccer")
data class PlayerPerformanceSoccer (
    val started: Boolean,
    var goals: Int? = 0,
    var assists: Int? = 0,
    var yellowCards: Int? = 0,
    var redCards: Int? = 0,
    var cleanSheet: Boolean, // manually calculated in game service, so no need to be nullable
    var saves: Int? = 0,
    var penaltySaves: Int? = 0,
    var penaltyMisses: Int? = 0,
    var ownGoals: Int = 0,

    override val player: Player,
    override val game: Game,
    override var minutes: Int?,
    @Id 
    override val id : ObjectId = ObjectId.get(),
) : PlayerPerformance()
    