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
    var complete: Boolean = false)


enum class TaskType{
    CheckGameSchedule,
    ScheduleDraft,
    GetScoresForFixture
}