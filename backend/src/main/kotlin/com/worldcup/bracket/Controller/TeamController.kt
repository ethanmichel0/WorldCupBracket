package com.worldcup.bracket.Controller

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

import com.worldcup.bracket.repository.TeamRepository
import com.worldcup.bracket.repository.GameRepository
import com.worldcup.bracket.DTO.TeamWithUpcomingGames
import com.worldcup.bracket.Service.GameService
import org.springframework.web.bind.annotation.PathVariable

import java.util.Optional

// TODO add other response codes besides 200
@RestController
class TeamController(
    private val teamRepository: TeamRepository, 
    private val gameService: GameService) {
    @GetMapping("/api/{teamId}")
    fun getTeam(@PathVariable teamId: String) : TeamWithUpcomingGames{
        return TeamWithUpcomingGames(
            teamRepository.findById(teamId).unwrap(),
            gameService.getAllGamesFromTeamUpcoming(teamId),
            gameService.getAllGamesFromTeamPast(teamId)
        )
    }
}

fun <T> Optional<T>.unwrap(): T? = orElse(null)