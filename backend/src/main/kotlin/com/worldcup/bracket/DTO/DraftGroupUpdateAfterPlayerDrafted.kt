package com.worldcup.bracket.DTO

import com.worldcup.bracket.Entity.PlayerSeason

data class DraftGroupUpdateAfterPlayerDrafted(
    val playerSelected: PlayerSeason,
    val playersRemaining: List<PlayerSeason>
)