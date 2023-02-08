package com.worldcup.bracket.Entity

import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.annotation.Id

@Document(collection="scheduledtasks")
data class ScheduledTasks (
    val name: String, 
    val type: TaskType,
    @Id 
    var id: String, // returned from scheduling api (if server is shut down and restarted, this will need to be updated to new id)
    val startTime: Long, // unix timestamp
    val repeat: Int)


enum class TaskType{
    CheckGameSchedule,
    ScheduleDraft,
    GetScoresForFixture
}