package com.worldcup.bracket.Repository

import org.springframework.data.mongodb.repository.MongoRepository
import com.worldcup.bracket.Entity.ScheduledTask
import org.springframework.stereotype.Repository;

import org.springframework.data.mongodb.repository.Query;

interface ScheduledTaskRepository : MongoRepository<ScheduledTask,String>{
    fun findByRelatedEntityIn(relatedEntities : List<String>) : List<ScheduledTask>
    fun findByRelatedEntity(relatedEntity : String) : List<ScheduledTask>
    fun findByCompleteFalse() : List<ScheduledTask>
}