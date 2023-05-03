package com.worldcup.bracket.DTO 

import com.worldcup.bracket.Entity.TeamSeason
import com.worldcup.bracket.Entity.Game
import com.worldcup.bracket.Entity.PlayerSeason

data class TeamWithExtraInfo (
    val teamSeasons : List<TeamSeason>,
    val upcomingGames : List<Game>,
    val pastGames : List<Game>,
    val players : List<PlayerSeason>
)