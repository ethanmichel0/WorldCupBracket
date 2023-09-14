package com.worldcup.bracket.DTO

import com.worldcup.bracket.Entity.PlayerSeason
import com.worldcup.bracket.Entity.PlayerDraft
import com.worldcup.bracket.Entity.DraftGroup

data class GameWeekSelection(
    val formation: String,
    val startingPlayers: List<String>,
    val subPriority: List<String>
)