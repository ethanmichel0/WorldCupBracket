package com.worldcup.bracket.Entity
import org.springframework.data.mongodb.core.mapping.Document
import org.bson.types.ObjectId
import org.springframework.data.annotation.Id

@Document(collection="playerperformancessoccer")
data class PlayerPerformanceSoccer (
    val started: Boolean,
    override var minutes: Int?,

    var goals: Int? = 0,
    var assists: Int? = 0,
    var saves: Int? = 0,

    var totalPasses: Int? = 0,
    var keyPasses: Int? = 0,
    var accuratePasses: Int? = 0,

    var tackles: Int? = 0,
    var blocks: Int? = 0,
    var interceptions: Int? = 0,

    var totalDuels: Int? = 0,
    var duelsWon: Int? = 0,

    var dribblesAttempted: Int? = 0,
    var dribblesSuccesful: Int? = 0,
    var dribblesPast: Int? = 0,

    var foulsDrawn: Int? = 0,
    var foulsCommitted: Int? = 0,

    var yellowCards: Int? = 0,
    var redCards: Int? = 0,

    var penaltiesDrawn: Int? = 0,
    var penaltiesCommitted: Int? = 0,
    var penaltiesMissed: Int? = 0,
    var penaltiesSaved: Int? = 0,
    var penaltiesScored: Int? = 0,

    var cleanSheet: Boolean, // manually calculated in game service, so no need to be nullable
    var ownGoals: Int = 0,

    override val playerSeason: PlayerSeason,
    override val game: Game,
    @Id 
    override val id : ObjectId = ObjectId.get(),
) : PlayerPerformance()
    