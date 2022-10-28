package com.worldcup.bracket.DTO 

import com.worldcup.bracket.Entity.Team
import com.worldcup.bracket.Entity.Game
import com.worldcup.bracket.Entity.Player

data class TeamWithExtraInfo (
    val team : Team?,
    val upcomingGames : List<Game>,
    val pastGames : List<Game>,
    val players : List<Player>
)