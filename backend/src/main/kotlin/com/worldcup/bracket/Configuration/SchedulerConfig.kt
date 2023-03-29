package com.worldcup.bracket.Configuration

import org.springframework.scheduling.config.ScheduledTaskRegistrar
import org.springframework.scheduling.annotation.SchedulingConfigurer
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.TaskScheduler
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary


@Configuration
public class SchedulerConfig : SchedulingConfigurer {

    companion object {
        val POOL_SIZE = 1
    }

    @Primary
    @Bean
    public fun taskScheduler() : TaskScheduler {
        val scheduler = ThreadPoolTaskScheduler();
        scheduler.setPoolSize(SchedulerConfig.POOL_SIZE);
        scheduler.setThreadNamePrefix("ThreadSchedulerzzzz-");
        scheduler.initialize();
        return scheduler
    }
    
    override public fun configureTasks(taskRegistrar: ScheduledTaskRegistrar) {
        taskRegistrar.setScheduler(taskScheduler());
    }
 }