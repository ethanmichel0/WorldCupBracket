package com.worldcup.bracket.DTO 

import com.worldcup.bracket.Entity.Team
import com.worldcup.bracket.Entity.Game

data class TeamWithUpcomingGames (
    val team : Team?,
    val upcomingGames : List<Game>,
    val pastGames : List<Game>
)