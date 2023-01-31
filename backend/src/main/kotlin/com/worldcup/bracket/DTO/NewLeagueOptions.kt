package com.worldcup.bracket.DTO

import com.worldcup.bracket.Entity.Sport

data class NewLeagueOptions(
    val sport: Sport,
    val playoffs: Boolean
)