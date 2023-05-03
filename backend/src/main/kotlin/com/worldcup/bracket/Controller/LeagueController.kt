package com.worldcup.bracket.Controller

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity

import com.worldcup.bracket.DTO.NewLeagueOptions
import com.worldcup.bracket.Entity.TeamSeason
import com.worldcup.bracket.Repository.TeamSeasonRepository
import com.worldcup.bracket.Service.LeagueService


import org.slf4j.Logger
import org.slf4j.LoggerFactory



@RestController
class LeagueController(
    private val leagueService : LeagueService,
    private val teamSeasonRepository : TeamSeasonRepository) {

    private val logger : Logger = LoggerFactory.getLogger(javaClass)

    @PostMapping("/api/leagues/{leagueId}")
    fun addNewSeason(@PathVariable leagueId : String, @RequestBody newLeagueOptions: NewLeagueOptions) : ResponseEntity<String> {
        try {
            leagueService.addNewSeasonForLeague(leagueId, newLeagueOptions)
            return ResponseEntity.status(HttpStatus.OK).body("")
        } catch (e: Exception) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.message)
        }
    }

    @GetMapping("/api/leagues/{leagueId}/{season}")
    fun getStandingsInLeagueForSeason(@PathVariable leagueId: String, @PathVariable season: Int) : ResponseEntity<List<TeamSeason>> {
        try {
            val standings = leagueService.getLeagueStandingsForSeason(leagueId,season)
            return ResponseEntity.status(HttpStatus.OK).body(standings)
        } catch (e: Exception) {
            if (e.message == LeagueService.INCORRECT_LEAGUE_ID_OR_SEASON) return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null)
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null)
        }
    }
}