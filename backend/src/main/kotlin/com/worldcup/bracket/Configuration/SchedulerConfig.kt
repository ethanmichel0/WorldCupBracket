package com.worldcup.bracket.Configuration

import org.springframework.scheduling.config.ScheduledTaskRegistrar
import org.springframework.scheduling.annotation.SchedulingConfigurer
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.TaskScheduler
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler
import org.springframework.context.annotation.Bean;


@Configuration
public class SchedulerConfig : SchedulingConfigurer {

    companion object {
        private val POOL_SIZE = 100
    }
    
    override public fun configureTasks(taskRegistrar: ScheduledTaskRegistrar) {
        val scheduler = ThreadPoolTaskScheduler();
        scheduler.setPoolSize(SchedulerConfig.POOL_SIZE);
        scheduler.setThreadNamePrefix("ThreadScheduler-");
        scheduler.initialize();
        taskRegistrar.setScheduler(scheduler);
    }
 }