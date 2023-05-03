package com.worldcup.bracket.Controller

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

import com.worldcup.bracket.Entity.PlayerPerformanceSoccer
import com.worldcup.bracket.Repository.PlayerPerformanceSoccerRepository

import org.springframework.web.bind.annotation.PathVariable
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.data.repository.findByIdOrNull

import java.util.Date

@RestController
class PlayerController(
    private val playerPerformanceSoccerRepository: PlayerPerformanceSoccerRepository
    ) {

    @GetMapping("/api/players/{playerId}")
    fun getAllPlayerPerformancesForPlayerBySeason(@PathVariable playerId: String) : List<PlayerPerformanceSoccer> {
        return playerPerformanceSoccerRepository.findAllPlayerPerformancesByPlayerDuringCurrentSeason(playerId)
    }
}