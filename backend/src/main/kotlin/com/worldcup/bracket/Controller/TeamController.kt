package com.worldcup.bracket.Controller

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController

import com.worldcup.bracket.Repository.TeamRepository
import com.worldcup.bracket.Repository.GameRepository
import com.worldcup.bracket.Repository.PlayerRepository
import com.worldcup.bracket.Repository.LeagueRepository
import com.worldcup.bracket.Entity.Team
import com.worldcup.bracket.DTO.OverrideGroupSettings
import com.worldcup.bracket.DTO.TeamWithExtraInfo
import com.worldcup.bracket.Service.GameService
import com.worldcup.bracket.Service.TeamService

import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity

import java.util.Optional
import java.util.Date

@RestController
class TeamController(
    private val teamRepository: TeamRepository, 
    private val gameRepository: GameRepository,
    private val playerRepository: PlayerRepository,
    private val leagueRepository: LeagueRepository,
    private val teamService: TeamService) {

        /* 
    @GetMapping("/api/leagues/{leagueId}/teams/{teamId}")
    fun getTeam(@PathVariable teamId: String) : ResponseEntity<TeamWithExtraInfo> {
        val team = teamRepository.findById(teamId).unwrap()

        if (team == null) return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null)
        return ResponseEntity.status(HttpStatus.OK).body(TeamWithExtraInfo(
            team,
            gameRepository.getAllGamesFromTeamUpcoming(teamId, System.currentTimeMillis()/1000),
            gameRepository.getAllGamesFromTeamPast(teamId, System.currentTimeMillis()/1000),
            playerSeasonRepository.findAllPlayerSeasonsForTeamCurrentlyActive(teamId: String))
    } */

    @GetMapping("/api/standings")
    fun getAllGroups() : List<Team> {
        return listOf<Team>()
        //return teamRepository.findByOrderByGroup()
    }

    @PostMapping("/api/groups/tieBreaker")
    fun overrideTieBreak(@RequestBody overrideGroupSettings : OverrideGroupSettings) : List<Team> {
        //return teamService.overrideGroupOrdering(overrideGroupSettings)
        return listOf<Team>()
    }
}

fun <T> Optional<T>.unwrap(): T? = orElse(null)