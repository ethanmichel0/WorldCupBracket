package com.worldcup.bracket.Service 

import org.springframework.stereotype.Service
import org.springframework.data.repository.findByIdOrNull

import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import com.google.gson.Gson; 
import com.google.gson.GsonBuilder;

import com.worldcup.bracket.DTO.FixturesAPIResponseWrapper
import com.worldcup.bracket.DTO.PlayersNested
import com.worldcup.bracket.DTO.AllEvents
import com.worldcup.bracket.Repository.GameRepository
import com.worldcup.bracket.Repository.TeamRepository
import com.worldcup.bracket.Repository.PlayerRepository
import com.worldcup.bracket.Repository.PlayerPerformanceRepository
import com.worldcup.bracket.Entity.Game
import com.worldcup.bracket.Entity.Team
import com.worldcup.bracket.Entity.Player
import com.worldcup.bracket.Entity.PlayerPerformance
import com.worldcup.bracket.Service.BuildNewRequest
import com.worldcup.bracket.FootballAPIData

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.util.Comparator

@Service
class GameService(private val gameRepository : GameRepository,
    private val teamRepository : TeamRepository,
    private val playerRepository : PlayerRepository,
    private val playerPerformanceRepository : PlayerPerformanceRepository,
    private val footballAPIData : FootballAPIData) {

    private val logger : Logger = LoggerFactory.getLogger(javaClass)

    public fun updateScores(fixtureId : String) {
        val request = BuildNewRequest(footballAPIData.setSingleFixtureAPI(fixtureId),"GET",null,"x-rapidapi-host",footballAPIData.X_RAPID_API_HOST,"x-rapidapi-key",footballAPIData.FOOTBALL_API_KEY)
        val response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
        val responseWrapper : FixturesAPIResponseWrapper = Gson().fromJson(response.body(), FixturesAPIResponseWrapper::class.java)
        val game : Game? = gameRepository.findByIdOrNull(responseWrapper.response[0].fixture.id)
        if (game != null) {
                if (responseWrapper.response[0].goals.home != null && responseWrapper.response[0].goals.away != null) {
                    game.homeScore = responseWrapper.response[0].goals.home!!
                    game.awayScore = responseWrapper.response[0].goals.away!!
                    game.currentMinute = responseWrapper.response[0].fixture.status.elapsed!!
                    setPlayerStatistics(
                        responseWrapper.response[0].players!![0].players + responseWrapper.response[0].players!![1].players,
                        responseWrapper.response[0].events!!,
                        game,
                        )
                }
                if (responseWrapper.response[0].fixture.status.long == footballAPIData.STATUS_FINISHED && ! game.scoresAlreadySet) {
                    if (game.knockoutGame) {
                        game.home.goalsForKnockout += responseWrapper.response[0].goals.home!!
                        game.away.goalsForKnockout += responseWrapper.response[0].goals.away!!
                        game.home.goalsAgainstKnockout += responseWrapper.response[0].goals.away!!
                        game.away.goalsAgainstKnockout += responseWrapper.response[0].goals.home!!

                        if (responseWrapper.response[0].goals.home!! > responseWrapper.response[0].goals.away!! ||
                                responseWrapper.response[0].score.penalty.home!! > responseWrapper.response[0].score.penalty.away!!) {
                                    game.winner = game.home
                                    game.home.winsKnockout ++
                                    game.away.lossesKnockout ++
                                } else {
                                    game.winner = game.away
                                    game.away.winsKnockout ++
                                    game.home.lossesKnockout ++
                                }
                    } else {
                        game.home.goalsForGroup += responseWrapper.response[0].goals.home!!
                        game.away.goalsForGroup += responseWrapper.response[0].goals.away!!
                        game.home.goalsAgainstGroup += responseWrapper.response[0].goals.away!!
                        game.away.goalsAgainstGroup += responseWrapper.response[0].goals.home!!
                        if (responseWrapper.response[0].goals.home!! > responseWrapper.response[0].goals.away!!) {
                            game.winner = game.home
                            game.home.winsGroup ++
                            game.away.lossesGroup ++
                        } else if (responseWrapper.response[0].goals.home!! < responseWrapper.response[0].goals.away!!) {
                            game.winner = game.away
                            game.away.winsGroup ++
                            game.home.lossesGroup ++;
                        } else {
                            game.home.ties ++
                            game.away.ties ++
                        }
                    }
                    game.scoresAlreadySet = true;
                }
                gameRepository.save(game)
                teamRepository.saveAll(listOf(game.home,game.away))
        }
    }

    private fun createKnockoutRound(round : String) {
        when (round) {
            "8th Finals" -> {
                val request = BuildNewRequest("${footballAPIData.FIXTURES_API}&round = 8th Finals","GET",null,"x-rapidapi-host",footballAPIData.X_RAPID_API_HOST,"x-rapidapi-key",footballAPIData.FOOTBALL_API_KEY)
                val firstPlaceTeams = teamRepository.findByPositionGroupOrderByGroupAsc(1);
                val secondPlaceTeams = teamRepository.findByPositionGroupOrderByGroupAsc(2);
                val gamesToCreate = mutableListOf<Game>()
                val response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
                val responseWrapper : FixturesAPIResponseWrapper = Gson().fromJson(response.body(), FixturesAPIResponseWrapper::class.java)
                
                findAndAddRelevantKnockoutGame(firstPlaceTeams[0],secondPlaceTeams[1],responseWrapper,gamesToCreate,"8th Finals",0)
                findAndAddRelevantKnockoutGame(firstPlaceTeams[1],secondPlaceTeams[0],responseWrapper,gamesToCreate,"8th Finals",1)
                findAndAddRelevantKnockoutGame(firstPlaceTeams[2],secondPlaceTeams[3],responseWrapper,gamesToCreate,"8th Finals",2)
                findAndAddRelevantKnockoutGame(firstPlaceTeams[3],secondPlaceTeams[2],responseWrapper,gamesToCreate,"8th Finals",3)
                findAndAddRelevantKnockoutGame(firstPlaceTeams[4],secondPlaceTeams[5],responseWrapper,gamesToCreate,"8th Finals",4)
                findAndAddRelevantKnockoutGame(firstPlaceTeams[5],secondPlaceTeams[4],responseWrapper,gamesToCreate,"8th Finals",5)
                findAndAddRelevantKnockoutGame(firstPlaceTeams[6],secondPlaceTeams[7],responseWrapper,gamesToCreate,"8th Finals",6)
                findAndAddRelevantKnockoutGame(firstPlaceTeams[7],secondPlaceTeams[6],responseWrapper,gamesToCreate,"8th Finals",7)

                gameRepository.saveAll(gamesToCreate)
            }
            "Quarter-Finals" -> {
                val request = BuildNewRequest("${footballAPIData.FIXTURES_API}&round = Quarter-Finals","GET",null,"x-rapidapi-host",footballAPIData.X_RAPID_API_HOST,"x-rapidapi-key",footballAPIData.FOOTBALL_API_KEY)
                val gamesToCreate = mutableListOf<Game>()
                val response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
                val responseWrapper : FixturesAPIResponseWrapper = Gson().fromJson(response.body(), FixturesAPIResponseWrapper::class.java)
                val winnersFromRoundOf16 = mutableListOf<Team>()
                val roundOf16Games = gameRepository.findByGroupOrderByGameNumberAsc("8th Finals");
                for (game in roundOf16Games) {
                    winnersFromRoundOf16.add(game.winner!!)
                }

                findAndAddRelevantKnockoutGame(winnersFromRoundOf16[0],winnersFromRoundOf16[1],responseWrapper,gamesToCreate,"Quarter-Finals",0)
                findAndAddRelevantKnockoutGame(winnersFromRoundOf16[2],winnersFromRoundOf16[3],responseWrapper,gamesToCreate,"Quarter-Finals",1)
                findAndAddRelevantKnockoutGame(winnersFromRoundOf16[4],winnersFromRoundOf16[5],responseWrapper,gamesToCreate,"Quarter-Finals",2)
                findAndAddRelevantKnockoutGame(winnersFromRoundOf16[6],winnersFromRoundOf16[7],responseWrapper,gamesToCreate,"Quarter-Finals",3)

                gameRepository.saveAll(gamesToCreate)
            }
            "Semi-Finals" -> {
                val request = BuildNewRequest("${footballAPIData.FIXTURES_API}&round = Semi-Finals","GET",null,"x-rapidapi-host",footballAPIData.X_RAPID_API_HOST,"x-rapidapi-key",footballAPIData.FOOTBALL_API_KEY)
                val gamesToCreate = mutableListOf<Game>()
                val response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
                val responseWrapper : FixturesAPIResponseWrapper = Gson().fromJson(response.body(), FixturesAPIResponseWrapper::class.java)
                val winnersFromQuarterFinals = mutableListOf<Team>()
                val quarterFinals = gameRepository.findByGroupOrderByGameNumberAsc("Quarter-Finals");
                for (game in quarterFinals) {
                    winnersFromQuarterFinals.add(game.winner!!)
                }

                findAndAddRelevantKnockoutGame(winnersFromQuarterFinals[0],winnersFromQuarterFinals[1],responseWrapper,gamesToCreate,"Semi-Finals",0)
                findAndAddRelevantKnockoutGame(winnersFromQuarterFinals[2],winnersFromQuarterFinals[3],responseWrapper,gamesToCreate,"Semi-Finals",1)
                
                gameRepository.saveAll(gamesToCreate)
            }
            "Final" -> {
                val request = BuildNewRequest("${footballAPIData.FIXTURES_API}&round = Final","GET",null,"x-rapidapi-host",footballAPIData.X_RAPID_API_HOST,"x-rapidapi-key",footballAPIData.FOOTBALL_API_KEY)
                val response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
                val responseWrapper : FixturesAPIResponseWrapper = Gson().fromJson(response.body(), FixturesAPIResponseWrapper::class.java)
                val winnersFromSemiFinals = mutableListOf<Team>()
                val semiFinals = gameRepository.findByGroupOrderByGameNumberAsc("Semi-Finals");
                for (game in semiFinals) {
                    winnersFromSemiFinals.add(game.winner!!)
                }

                gameRepository.save(
                    Game(winnersFromSemiFinals[0],
                        winnersFromSemiFinals[1],
                        "Final",
                        true,
                        responseWrapper.response[0].fixture.timestamp.toLong(),
                        responseWrapper.response[0].fixture.id,
                        1))
            }
        }
    }

    private fun findAndAddRelevantKnockoutGame(home : Team, away : Team, wrapper : FixturesAPIResponseWrapper, games : MutableList<Game>, round : String, gameNum : Int) {
        val game = wrapper.response.filter{(it.teams.home == home && 
                            it.teams.away == away)}[0]
                games.add(
                    Game(home,
                        away,
                        round,
                        true,
                        game.fixture.timestamp.toLong(),
                        game.fixture.id,
                        gameNum))
    }

    private fun setPlayerStatistics(allPlayersBothTeams: List<PlayersNested>, allEvents: List<AllEvents>, relatedGame: Game) {
        logger.info("setting stats")
        val homeTeamId = relatedGame.home.id!!
        val awayTeamId = relatedGame.away.id!!
        val allPlayersInSameGameFromRepo = playerRepository.findAllPlayersFromOneGame(homeTeamId,awayTeamId)

        // we will create a new player performance the first time entering this method, otherwise will use existing records
        // this is why it is declared as a mutable list even though when retrieved from the database it will not be used as a mutable list,
        // only used as mutable list the first time when we are creating player performances
        val playerPerformances : MutableList<PlayerPerformance> = playerPerformanceRepository.findAllPlayerPerformancesByGame(relatedGame.fixtureId).toMutableList()
        val firstTimeCreatingPerformances = playerPerformances.size == 0
        

        allPlayersBothTeams.forEach{playerFromAPI ->
            val playerFromRepo = allPlayersInSameGameFromRepo.filter{playerFromRepo -> playerFromRepo.id == playerFromAPI.player.id}[0]
            if (firstTimeCreatingPerformances) {
                playerPerformances.add(
                    PlayerPerformance(
                        player=playerFromRepo,
                        game = relatedGame,
                        minutes = playerFromAPI.statistics[0].games.minutes,
                        started = ! (playerFromAPI.statistics[0].games.substitute),
                        goals = playerFromAPI.statistics[0].goals.total,
                        assists = playerFromAPI.statistics[0].goals.assists,
                        yellowCards = playerFromAPI.statistics[0].cards.yellow,
                        redCards = playerFromAPI.statistics[0].cards.red,
                        saves = playerFromAPI.statistics[0].goals.saves,
                        cleanSheet = if (playerFromRepo.team == relatedGame.home) relatedGame.awayScore == 0 else relatedGame.homeScore == 0,
                        penaltySaves = playerFromAPI.statistics[0].penalty.saved,
                        penaltyMisses = playerFromAPI.statistics[0].penalty.missed
                    )
                )
            } else {
                val relevantPlayerPerformance = playerPerformances.filter{pp -> pp.player.id == playerFromAPI.player.id}[0];
                relevantPlayerPerformance.minutes = playerFromAPI.statistics[0].games.minutes
                relevantPlayerPerformance.goals = playerFromAPI.statistics[0].goals.total
                relevantPlayerPerformance.assists = playerFromAPI.statistics[0].goals.assists
                relevantPlayerPerformance.yellowCards = playerFromAPI.statistics[0].cards.yellow
                relevantPlayerPerformance.redCards = playerFromAPI.statistics[0].cards.red
                relevantPlayerPerformance.saves = playerFromAPI.statistics[0].goals.saves
                relevantPlayerPerformance.cleanSheet = if (playerFromRepo.team == relatedGame.home) relatedGame.awayScore == 0 else relatedGame.homeScore == 0
                relevantPlayerPerformance.penaltySaves = playerFromAPI.statistics[0].penalty.saved
                relevantPlayerPerformance.penaltyMisses = playerFromAPI.statistics[0].penalty.missed
            }
        }

        allEvents
            .forEach{event ->
                // the api marks own goals as an event but not under each player as an individual statistic.
                // thus we must filter to find the event and add it to the corresponding player 
                if (event.detail == "Own Goal") {
                    val allRelevantPerformances = playerPerformances.filter{pp -> pp.player.id == event.player.id}
                    allRelevantPerformances[0].ownGoals ++
                }
            }

        playerPerformanceRepository.saveAll(playerPerformances)

        if (relatedGame.scoresAlreadySet) { // indicates that the came is over and we can increment cummulative player stats
            val playersToUpdate = mutableListOf<Player>()
            playerPerformances.forEach{
                pp -> 
                var markedForUpdate = false
                if (pp.goals != null && pp.goals!! > 0) {
                    pp.player.goals += pp.goals!!
                    if (! markedForUpdate) {
                        playersToUpdate.add(pp.player)
                        markedForUpdate = true
                    }
                }
                if (pp.assists != null && pp.assists!! > 0) {
                    pp.player.assists += pp.assists!!
                    if (! markedForUpdate) {
                        playersToUpdate.add(pp.player)
                        markedForUpdate = true
                    }
                }
            }

            // more cummulative updates here

            playerRepository.saveAll(playersToUpdate)
        }

    }
}