package com.worldcup.bracket.Entity

import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.annotation.Id
import org.bson.types.ObjectId

import java.time.Duration

@Document(collection="scheduledtasks")
data class ScheduledTask (
    val type: TaskType,
    @Id 
    var id : ObjectId = ObjectId.get(), // returned from scheduling api (if server is shut down and restarted, this will need to be updated to new id)
    val startTime: Long, // unix timestamp
    val repeat: Duration?, // null indicates it is a one time job
    val relatedEntity: String,
    val season: Int? = null, // used for daily schedule check in different leagues, which may have different season (e.g. March 2023 is Premier League "2022" season as categorized in API
    // but MLS 2023 season)
    val dateString: String? = null,
    var complete: Boolean = false)


enum class TaskType{
    CheckGameSchedule,
    ScheduleDraft,
    GetScoresForFixture
}