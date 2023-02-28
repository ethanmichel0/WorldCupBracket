package com.worldcup.bracket.Entity

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

import org.bson.types.ObjectId


@Document(collection="leagues")
data class League(
    @Id 
    val id: String,
    val name: String,
    val country: String,
    val sport: Sport,
    val playoffs: Boolean,
    val logo: String,
    val scheduleType: ScheduleType,
    val lastDateToDraft: Long? = null
)

enum class Sport {
    Soccer
}

enum class ScheduleType {
    AllInSameYear, // this is for leagues like the MLS, or tournaments like World Cup that go beginning of year to end of year
    ThroughSpringOfNextYear, // this is for leagues like the Premier League that go fall to spring of next year
}
    