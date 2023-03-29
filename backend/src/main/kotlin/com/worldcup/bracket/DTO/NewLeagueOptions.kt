package com.worldcup.bracket.DTO

import com.worldcup.bracket.Entity.Sport
import com.worldcup.bracket.Entity.ScheduleType

data class NewLeagueOptions(
    val sport: Sport,
    val playoffs: Boolean,
    val scheduleType: ScheduleType,
    val leagueIdWhoScored: String 
)

/// whoScored.com allows us to incorporate extra statistics such as goals outside of box and errors leading to goal