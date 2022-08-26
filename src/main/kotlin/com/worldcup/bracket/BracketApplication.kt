package com.worldcup.bracket

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

import org.springframework.data.mongodb.repository.config.EnableMongoRepositories

@EnableMongoRepositories
@SpringBootApplication
class BracketApplication

fun main(args: Array<String>) {
	runApplication<BracketApplication>(*args)
}