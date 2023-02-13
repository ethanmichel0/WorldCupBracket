package com.worldcup.bracket.Service

import org.springframework.scheduling.TaskScheduler
import org.springframework.stereotype.Service
import java.time.Duration
import java.time.Instant
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.atomic.AtomicInteger

import com.worldcup.bracket.Entity.ScheduledTask
import com.worldcup.bracket.Entity.TaskType
import com.worldcup.bracket.Repository.ScheduledTaskRepository

import org.springframework.data.repository.findByIdOrNull

@Service
class SchedulerService(private val scheduler: TaskScheduler, private val scheduledTaskRepository : ScheduledTaskRepository) {
    val futures: MutableMap<String, ScheduledFuture<*>> = HashMap()

    // note that task is not added to the db in method so that if we want to add several tasks to scheduler at once
    // we can call this method several times w/o performance issues
    fun addNewTask(task: Runnable, startTime: Instant, repeatEvery: Duration?, type: TaskType, relatedEntity: String): ScheduledTask { // use -1 for no repeat
        val scheduledTaskFuture = if (repeatEvery != null) scheduler.scheduleAtFixedRate(task, startTime, repeatEvery) else scheduler.schedule(task,startTime)

        val scheduledTask = ScheduledTask(
            repeat = -1,
            startTime = 1,
            type = type,
            relatedEntity = relatedEntity
        )

        futures[scheduledTask.toString()] = scheduledTaskFuture
        return scheduledTask
    }

    // note that task is not removed from this db in method so that if we want to remove several tasks from scheduler at once
    // we can call this method several times w/o performance issues
    fun removeTaskFromScheduler(id: String) : ScheduledTask {
        futures[id]?.let {
            it.cancel(true)
            futures.remove(id)
            val relevantTaskDB = scheduledTaskRepository.findByIdOrNull(id)!!
            return relevantTaskDB
        }
        throw Exception("Scheduled task to be removed with id: ${id} not found")
    }

    // note that task is not saved to db in method so that if we want to mark several tasks at complete at once
    // we can call this method several times w/o performance issues
    fun markTaskAsComplete(id: String) : ScheduledTask {
        futures[id]?.let {
            val relevantTaskDB = scheduledTaskRepository.findByIdOrNull(id)!!
            relevantTaskDB.complete = true
            return relevantTaskDB
        }
        throw Exception("Scheduled task to be marked as complete with id: ${id} not found")
    }
}