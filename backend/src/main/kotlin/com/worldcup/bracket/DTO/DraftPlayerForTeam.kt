package com.worldcup.bracket.DTO

import com.worldcup.bracket.Entity.PlayerSeason

data class DraftPlayerForTeam(
    val playerId: String,
    val draftGroupName: String
)