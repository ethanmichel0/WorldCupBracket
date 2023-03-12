package com.worldcup.bracket.DTO

import com.worldcup.bracket.Entity.Sport
import com.worldcup.bracket.Entity.ScheduleType

data class NewLeagueOptions(
    val sport: Sport,
    val playoffs: Boolean,
    val scheduleType: ScheduleType
)

// var espnId: String, TODO This may be added in the future as ESPN allows certain stastistics such as goals out of box and free kicks that
// are not available otherwise