package com.worldcup.bracket.Controller

import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity

import com.worldcup.bracket.Service.LeagueService
import com.worldcup.bracket.DTO.NewLeagueOptions


import org.slf4j.Logger
import org.slf4j.LoggerFactory



@RestController
class LeagueController(private val leagueService : LeagueService) {

    private val logger : Logger = LoggerFactory.getLogger(javaClass)

    @PostMapping("/api/league/{leagueId}")
    fun addNewSeason(@PathVariable leagueId : String, @RequestBody newLeagueOptions: NewLeagueOptions) : ResponseEntity<String> {
        leagueService.addNewSeasonForLeague(leagueId, newLeagueOptions)
    }
}