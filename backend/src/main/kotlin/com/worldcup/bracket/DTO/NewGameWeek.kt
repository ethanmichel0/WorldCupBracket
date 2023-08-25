package com.worldcup.bracket.DTO

data class NewGameWeek(
    val league: String,
    var start: Long,
    var end: Long,
    var deadline: Long = start - (24*60*60),
    val gameWeekName: String
)