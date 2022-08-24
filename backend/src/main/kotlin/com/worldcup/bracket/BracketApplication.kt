package com.worldcup.bracket

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

fun main(args: Array<String>) {
	runApplication<BracketApplication>(*args)
}

@SpringBootApplication
class BracketApplication