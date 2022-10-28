package com.worldcup.bracket.Entity

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document(collection="playerperformances")
data class PlayerPerformance(
    val player: Player,
    val game: Game,
    var goals: Int,
    var assists: Int,
    var cleanSheet: Boolean,
    var rating: Int,
    var minutes: Int,
    var yellowCard: Boolean,
    var redCard: Boolean,
    val saves : Int?
    ) {
    }
    