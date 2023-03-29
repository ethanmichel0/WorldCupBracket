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
    public val futures: MutableMap<String, ScheduledFuture<*>> = HashMap()

    // note that task is not added to the db in method so that if we want to add several tasks to scheduler at once
    // we can call this method several times w/o performance issues
    // addToDb option should be false if we are restarting server and scheduled task was already added to db from last time server was up
    fun addNewTask(task: Runnable, startTime: Instant, repeatEvery: Duration?, type: TaskType, relatedEntity: String, addToDB: Boolean=true): ScheduledTask { // use -1 for no repeat
        val markTaskAsCompleteRunnable = Runnable {
            val relevantTask = scheduledTaskRepository.findByRelatedEntity(relatedEntity)[0]
            relevantTask.complete = true
            scheduledTaskRepository.save(relevantTask)
        }

        val doTaskAndMarkAsCompleteRunnable = Runnable {
            task.run()
            markTaskAsCompleteRunnable.run()
        }

        // if task is only run once (repeatEvery is not provided) mark task as complete. If it repeats, will need to instead use removeTaskFromScheduler since taskScheduler doesn't
        // provide way to specify when task will stop repeating
        val scheduledTaskFuture = if (repeatEvery != null) scheduler.scheduleAtFixedRate(task, startTime, repeatEvery) else scheduler.schedule(doTaskAndMarkAsCompleteRunnable,startTime)

        if (addToDB) {
            val scheduledTask = ScheduledTask(
            repeat = repeatEvery,
            startTime = startTime.getEpochSecond(),
            type = type,
            relatedEntity = relatedEntity
            )
            futures[scheduledTask.id.toString()] = scheduledTaskFuture
            return scheduledTask
        } else {
            val scheduledTask = scheduledTaskRepository.findByRelatedEntity(relatedEntity)[0]!!
            futures[scheduledTask.id.toString()] = scheduledTaskFuture
            return scheduledTask
        }
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

    // by default, repeated tasks repeat forever, so this will allow us to cancel. (for example at end of season we can stop checking for
    // new games/games being rescheduled)
    fun markTaskAsComplete(id: String) : ScheduledTask {
        futures[id]?.let {
            val relevantTaskDB = scheduledTaskRepository.findByIdOrNull(id)!!
            relevantTaskDB.complete = true
            futures.remove(id)
            return relevantTaskDB
        }
        throw Exception("Scheduled task to be marked as complete with id: ${id} not found")
    }


}

// TODO figure out better system for removing scheduled tasks