package com.worldcup.bracket.DTO

import com.worldcup.bracket.Entity.Sport
import com.worldcup.bracket.Entity.ScheduleType

data class NewLeagueOptions(
    val sport: Sport,
    val playoffs: Boolean,
    val scheduleType: ScheduleType
)