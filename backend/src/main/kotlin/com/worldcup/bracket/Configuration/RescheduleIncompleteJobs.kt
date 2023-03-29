package com.worldcup.bracket.Configuration

import org.springframework.stereotype.Component
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner

import java.time.Instant
import java.time.Duration

import com.worldcup.bracket.Entity.TaskType
import com.worldcup.bracket.Repository.ScheduledTaskRepository
import com.worldcup.bracket.Service.GameService
import com.worldcup.bracket.Service.SchedulerService




@Component
public class RescheduleIncompleteJobs (
    private val scheduledTaskRepository: ScheduledTaskRepository,
    private val gameService: GameService,
    private val schedulerService: SchedulerService
) : ApplicationRunner {

    override fun run (args: ApplicationArguments) {
        
        this.scheduledTaskRepository.findByCompleteFalse().forEach{
            if (it.type == TaskType.GetScoresForFixture) {
                schedulerService.addNewTask(
                    task = Runnable {gameService.updateScores(it.relatedEntity)},
                    startTime = Instant.ofEpochSecond(it.startTime),
                    repeatEvery = null,
                    type = TaskType.GetScoresForFixture,
                    relatedEntity = it.relatedEntity,
                    addToDB = false
                )
            } else if (it.type == TaskType.CheckGameSchedule) {
                val scheduleTask = schedulerService.addNewTask(
                    task = Runnable {
                        gameService.setLeagueGames(it.relatedEntity,it.season!!)
                        },
                    startTime = Instant.ofEpochSecond(it.startTime),
                    repeatEvery = Duration.ofDays(1),
                    type = TaskType.CheckGameSchedule,
                    relatedEntity = it.relatedEntity,
                    addToDB = false
                )
            }
        }
    }
}