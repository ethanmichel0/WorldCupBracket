package com.worldcup.bracket.DTO

data class NewDraftGroup(
    val name: String,
    val password: String,
    val leagueIds: List<String> = listOf<String>("39"),
    val numPlayers: Int = 17,
    val amountTimePerTurnInSeconds: Int = 60
)