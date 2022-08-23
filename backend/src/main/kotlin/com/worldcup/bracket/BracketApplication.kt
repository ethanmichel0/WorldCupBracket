package com.worldcup.bracket

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.boot.ApplicationRunner

@SpringBootApplication
class BracketApplication

fun main(args: Array<String>) {
	runApplication<BracketApplication>(*args)
}