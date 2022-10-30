package com.worldcup.bracket.DTO

import com.worldcup.bracket.Entity.Position

data class PlayersAPIResponseWrapper(
    val response : List<OuterWrapper>
)

data class OuterWrapper(
    val player: PlayersWrapper,
    val statistics: List<StatisticsWrapper>
)

data class PlayersWrapper(
    val name : String,
    val height: String,
    val weight: String,
    val age: Int,
    val id: Int
)

data class StatisticsWrapper(
    val games : GamesWrapper,
)

data class GamesWrapper(
    val position: Position,
)