package com.worldcup.bracket.Controller

import org.springframework.data.repository.findByIdOrNull
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.http.ResponseEntity
import org.springframework.http.HttpStatus

import com.worldcup.bracket.Service.GameService
import com.worldcup.bracket.Entity.Game

import com.worldcup.bracket.Repository.GameRepository


import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;


@RestController
class GameController(
    private val gameService: GameService,
    private val gameRepository: GameRepository
    ) {
    @GetMapping("/api/games/{leagueId}/")
    fun getUpcomingGamesInLeague(@PathVariable leagueId: String) : ResponseEntity<List<Game>> {
        try {
            val upcomingGames = gameService.getUpcomingGamesInLeague(leagueId)
            return ResponseEntity.status(HttpStatus.OK).body(upcomingGames)
        } catch (e: Exception) {
            if (e.message == GameService.LEAGUE_DOES_NOT_EXIST) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null)
            }
            if (e.message == GameService.NO_UPCOMING_GAMES_FOR_LEAGUE) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null)
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null)
        }
    }

    @GetMapping("/api/games/{fixtureId}")
    fun getAllInfoForGame(@PathVariable fixtureId: String) : Game {
        return gameRepository.findByIdOrNull(fixtureId)!!
    }

    @MessageMapping("/api/games/testwebsocket")
    @SendTo("/topic/greetings")
    fun greet(@Payload message: String) : String {
        println("greet function hit!")
        return "Hello, ${message}! How are you?"
    }
}