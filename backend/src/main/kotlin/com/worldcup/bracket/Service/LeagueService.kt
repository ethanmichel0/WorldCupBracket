package com.worldcup.bracket.Service 

import org.springframework.stereotype.Service
import org.springframework.data.repository.findByIdOrNull

import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

import com.worldcup.bracket.DTO.LeagueResponse
import com.worldcup.bracket.DTO.NewLeagueOptions
import com.worldcup.bracket.DTO.StandingsResponse
import com.worldcup.bracket.DTO.PlayersAPIResponseWrapper

import com.worldcup.bracket.Repository.LeagueRepository
import com.worldcup.bracket.Repository.TeamSeasonRepository
import com.worldcup.bracket.Repository.TeamRepository
import com.worldcup.bracket.Repository.PlayerRepository
import com.worldcup.bracket.Repository.PlayerSeasonRepository

import com.worldcup.bracket.Entity.League
import com.worldcup.bracket.Entity.Team
import com.worldcup.bracket.Entity.TeamSeason
import com.worldcup.bracket.Entity.Player
import com.worldcup.bracket.Entity.PlayerSeason

import com.worldcup.bracket.FootballAPIData

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import com.google.gson.Gson; 
import com.google.gson.GsonBuilder;

import kotlinx.coroutines.*

@Service
class LeagueService(
    private val leagueRepository : LeagueRepository,
    private val teamSeasonRepository : TeamSeasonRepository,
    private val teamRepository : TeamRepository,
    private val playerRepository : PlayerRepository,
    private val playerSeasonRepository : PlayerSeasonRepository,
    private val footballAPIData : FootballAPIData) {
    
    val httpClient = HttpClient.newHttpClient()
    private val logger : Logger = LoggerFactory.getLogger(javaClass)


    public fun addNewSeasonForLeague(leagueId: String, newLeagueOptions: NewLeagueOptions) {
        val relevantLeagueFromDB = leagueRepository.findByIdOrNull(leagueId)
        
        val requestLeague = BuildNewRequest(footballAPIData.setLeagueAPI(leagueId),"GET",null,"x-rapidapi-host",footballAPIData.X_RAPID_API_HOST,"x-rapidapi-key",footballAPIData.FOOTBALL_API_KEY)
        val responseLeague = httpClient.send(requestLeague, HttpResponse.BodyHandlers.ofString());
        val responseWrapperLeague : LeagueResponse = Gson().fromJson(responseLeague.body(), LeagueResponse::class.java)

        var firstTimeAddingTeamThisSeason = false // this distinguishes between player transfers mid season and the first time getting rosters at beginning of season
        
        val seasons = responseWrapperLeague.response[0].seasons
        val latestSeason = seasons[seasons.lastIndex].year
        if (relevantLeagueFromDB == null) {
            val relevantLeagueFromDB = League(
                    name=responseWrapperLeague.response[0].league.name,
                    id=responseWrapperLeague.response[0].league.id,
                    logo=responseWrapperLeague.response[0].league.logo,
                    country=responseWrapperLeague.response[0].country.name,
                    sport=newLeagueOptions.sport,
                    playoffs=newLeagueOptions.playoffs
            )
            leagueRepository.save(
                relevantLeagueFromDB
            )
            
        }

        val allRelevantTeamSeasons = teamSeasonRepository.findAllTeamSeasonsBySeasonAndLeague(latestSeason,responseWrapperLeague.response[0].league.name)
        if (allRelevantTeamSeasons.size == 0) {
            firstTimeAddingTeamThisSeason = true
            val requestStandings = BuildNewRequest(footballAPIData.setStandingsAPI(leagueId,latestSeason),"GET",null,"x-rapidapi-host",footballAPIData.X_RAPID_API_HOST,"x-rapidapi-key",footballAPIData.FOOTBALL_API_KEY)
            val responseStandings = httpClient.send(requestStandings, HttpResponse.BodyHandlers.ofString());
            val responseWrapperStandings : StandingsResponse = Gson().fromJson(responseStandings.body(), StandingsResponse::class.java)

            val teamSeasons = mutableListOf<TeamSeason>()
            val teams = mutableListOf<Team>()
            for (teamInfo in responseWrapperStandings.response[0].league.standings) {
                val relevantTeam = Team(
                    name=teamInfo.team.name,
                    id=teamInfo.team.id,
                    logo=teamInfo.team.logo,
                    group=teamInfo.group
                )
                if (teamRepository.findByName(teamInfo.team.name).size==0) {
                    teams.add(relevantTeam)
                }

                teamSeasons.add(TeamSeason(
                    team = relevantTeam,
                    league = relevantLeagueFromDB!!,
                    season = latestSeason,
                    position = teamInfo.rank
                ))
            }
            teamSeasonRepository.saveAll(teamSeasons)
            teamRepository.saveAll(teams)
        }

        // add all players from each team

        val allPlayers = mutableListOf<Player>()
        val allPlayerSeasons = mutableListOf<PlayerSeason>()

        runBlocking {
            val allPlayersByTeam = mutableListOf<Deferred<Pair<List<Player>,List<PlayerSeason>>>>()
            allRelevantTeamSeasons.forEach {
                allPlayersByTeam.add(async(Dispatchers.IO) { 
                    addPlayersFromTeam(it,firstTimeAddingTeamThisSeason)
                })
            }

            for ((players,playerSeasons) in allPlayersByTeam.awaitAll()) {
                allPlayers.addAll(players)
                allPlayerSeasons.addAll(allPlayerSeasons)
            }
        }

        playerRepository.saveAll(allPlayers)
        playerSeasonRepository.saveAll(allPlayerSeasons)
    }


    private suspend fun addPlayersFromTeam(teamSeason: TeamSeason, firstTimeAddingTeamThisSeason: Boolean) : Pair<List<Player>,List<PlayerSeason>> {
        val playersRequest = BuildNewRequest(footballAPIData.getAllPlayersOnTeam(teamSeason.team.id!!),"GET",null,"x-rapidapi-host",footballAPIData.X_RAPID_API_HOST,"x-rapidapi-key",footballAPIData.FOOTBALL_API_KEY)
        val playersResponse = httpClient.send(playersRequest, HttpResponse.BodyHandlers.ofString());
        val playersResponseWrapper : PlayersAPIResponseWrapper = Gson().fromJson(playersResponse.body(), PlayersAPIResponseWrapper::class.java)
        
        val playersToAddToDB = mutableListOf<Player>()
        val playersSeasonsToAddToDB = mutableListOf<PlayerSeason>()

        for (player in playersResponseWrapper.response[0].players) {
            // check if player already in database
            val relevantPlayerToAddToDB = Player(
                name = player.name,
                id = player.id
            )

            val relevantPlayerSeason = PlayerSeason(
                player = relevantPlayerToAddToDB,
                teamSeason = teamSeason,
                position = player.position,
                number = player.number
            )

            if (playerRepository.findByIdOrNull(player.id)==null) {
                playersToAddToDB.add(relevantPlayerToAddToDB)
            }

            if(! firstTimeAddingTeamThisSeason) {
                
            }

            // add all players for beginning of season and all new transferred players after transfer breaks during the season
            if (firstTimeAddingTeamThisSeason || playerSeasonRepository.findAllPlayerSeasonsBySeasonAndPlayer(teamSeason.season,player.id).size == 0) {
                playersSeasonsToAddToDB.add(relevantPlayerSeason)
            }
        }

        // change "current" property to false on all players that transferred out of club during transfer windows if this is during transfer window
        // this can be done by checking all player ids of current squad from api and comparing against those in data base to confirm players are still playing for club
        
        if (!firstTimeAddingTeamThisSeason) {
            val allCurrentPlayerIds : List<String> = playersResponseWrapper.response[0].players.map{it.id}
            playerSeasonRepository.findAllPlayerSeasonsByTeamSeason(teamSeason.id).filter{! allCurrentPlayerIds.contains(it.player.id) }.
                forEach{
                    it.current = false
                    playersSeasonsToAddToDB.add(it)
                }
        }
        
        return Pair(playersToAddToDB,playersSeasonsToAddToDB)
    }
}