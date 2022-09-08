package com.worldcup.bracket.Controller

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

import com.worldcup.bracket.repository.GameRepository
import com.worldcup.bracket.Entity.Game

// TODO add other response codes besides 200

@RestController
class GameController(private val gameRepository: GameRepository) {
    @GetMapping("/api/games")
    fun getAllGamesSortByDateAsc() : List<Game> {
        return gameRepository.findByOrderByDateAsc()
    }
}