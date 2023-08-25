package com.worldcup.bracket

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

import org.springframework.data.mongodb.repository.config.EnableMongoRepositories
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer


import org.springframework.context.annotation.Configuration
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration
import  org.springframework.scheduling.annotation.EnableScheduling

@EnableMongoRepositories
@SpringBootApplication
@Configuration
@ConfigurationPropertiesScan
@EnableScheduling
class BracketApplication 

fun main(args: Array<String>) {
	runApplication<BracketApplication>(*args)
}