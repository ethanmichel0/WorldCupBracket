package com.worldcup.bracket.Controller

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController

import com.worldcup.bracket.Repository.PlayerSeasonRepository
import com.worldcup.bracket.Repository.GameRepository
import com.worldcup.bracket.Repository.TeamSeasonRepository
import com.worldcup.bracket.Entity.PlayerSeason
import com.worldcup.bracket.DTO.OverrideGroupSettings
import com.worldcup.bracket.DTO.TeamWithExtraInfo

import org.springframework.web.bind.annotation.PathVariable
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.data.repository.findByIdOrNull

import java.util.Date

@RestController
class TeamController(
    private val playerSeasonRepository: PlayerSeasonRepository,
    private val teamSeasonRepository: TeamSeasonRepository,
    private val gameRepository: GameRepository) {

    @GetMapping("/api/teams/{teamId}")
    fun getInfoForTeam(@PathVariable teamId: String) : TeamWithExtraInfo {
        val allCurentTeamSeasons = teamSeasonRepository.findAllCurrentTeamSeasonsByTeam(teamId)
        return TeamWithExtraInfo(
            teamSeasons = allCurentTeamSeasons,
            players = playerSeasonRepository.findAllPlayerSeasonsForTeamCurrentlyActive(teamId),
            upcomingGames = gameRepository.getAllGamesFromTeamUpcoming(teamId,Date().getTime()),
            pastGames = gameRepository.getAllGamesFromTeamPast(teamId,Date().getTime())
        )
    }
}