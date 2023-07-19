package com.worldcup.bracket.DTO 

import com.worldcup.bracket.Entity.PlayerSeason

data class UpdatedWatchList (
    val updatedWatchList : List<PlayerSeason>
)