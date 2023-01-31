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
import com.worldcup.bracket.Repository.PlayerPerformanceSoccerRepository
import com.worldcup.bracket.Entity.Game
import com.worldcup.bracket.Entity.Team
import com.worldcup.bracket.Entity.Player
import com.worldcup.bracket.Entity.PlayerPerformanceSoccer
import com.worldcup.bracket.Service.BuildNewRequest
import com.worldcup.bracket.FootballAPIData

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.util.Comparator

@Service
class GameService(private val gameRepository : GameRepository,
    private val teamRepository : TeamRepository,
    private val playerRepository : PlayerRepository,
    private val playerPerformanceRepository : PlayerPerformanceSoccerRepository,
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

    private fun setPlayerStatistics(allPlayersBothTeams: List<PlayersNested>, allEvents: List<AllEvents>, relatedGame: Game) {
        logger.info("setting stats")
        val homeTeamId = relatedGame.home.id!!
        val awayTeamId = relatedGame.away.id!!
        val allPlayersInSameGameFromRepo = playerRepository.findAllPlayersFromOneGame(homeTeamId,awayTeamId)

        // we will create a new player performance the first time entering this method, otherwise will use existing records
        // this is why it is declared as a mutable list even though when retrieved from the database it will not be used as a mutable list,
        // only used as mutable list the first time when we are creating player performances
        val playerPerformances : MutableList<PlayerPerformanceSoccer> = playerPerformanceRepository.findAllPlayerPerformancesByGame(relatedGame.fixtureId).toMutableList()
        val firstTimeCreatingPerformances = playerPerformances.size == 0
        

        allPlayersBothTeams.forEach{playerFromAPI ->
            val playerFromRepo = allPlayersInSameGameFromRepo.filter{playerFromRepo -> playerFromRepo.id == playerFromAPI.player.id}[0]
            if (firstTimeCreatingPerformances) {
                playerPerformances.add(
                    PlayerPerformanceSoccer(
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
                val relevantPlayerPerformanceSoccer = playerPerformances.filter{pp -> pp.player.id == playerFromAPI.player.id}[0];
                relevantPlayerPerformanceSoccer.minutes = playerFromAPI.statistics[0].games.minutes
                relevantPlayerPerformanceSoccer.goals = playerFromAPI.statistics[0].goals.total
                relevantPlayerPerformanceSoccer.assists = playerFromAPI.statistics[0].goals.assists
                relevantPlayerPerformanceSoccer.yellowCards = playerFromAPI.statistics[0].cards.yellow
                relevantPlayerPerformanceSoccer.redCards = playerFromAPI.statistics[0].cards.red
                relevantPlayerPerformanceSoccer.saves = playerFromAPI.statistics[0].goals.saves
                relevantPlayerPerformanceSoccer.cleanSheet = if (playerFromRepo.team == relatedGame.home) relatedGame.awayScore == 0 else relatedGame.homeScore == 0
                relevantPlayerPerformanceSoccer.penaltySaves = playerFromAPI.statistics[0].penalty.saved
                relevantPlayerPerformanceSoccer.penaltyMisses = playerFromAPI.statistics[0].penalty.missed
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