package com.worldcup.bracket.Entity

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

import org.bson.types.ObjectId


@Document(collection="gamepredictions")
data class GamePredictions(
    @Id 
    val id : ObjectId = ObjectId.get(),
    val game: Game, 
    var chosenWinner: Team?, 
    var bracket: Bracket,
    var correct: Boolean? = null
)
    