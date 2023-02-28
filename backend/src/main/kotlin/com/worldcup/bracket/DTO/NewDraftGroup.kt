package com.worldcup.bracket.DTO

data class NewDraftGroup(
    val name: String,
    val password: String,
    val leagueIds: List<String>,
    val numPlayers: Int? = 15,
    val amountTimePerTurnInSeconds: Int? = 60
)