package com.worldcup.bracket.Service 

import org.springframework.stereotype.Service
import org.springframework.data.repository.findByIdOrNull

import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import com.google.gson.Gson; 
import com.google.gson.GsonBuilder;

import com.worldcup.bracket.DTO.FixturesAPIResponseWrapper
import com.worldcup.bracket.DTO.AllEvents
import com.worldcup.bracket.DTO.PlayersNested

import com.worldcup.bracket.Repository.GameRepository
import com.worldcup.bracket.Repository.TeamSeasonRepository
import com.worldcup.bracket.Repository.LeagueRepository
import com.worldcup.bracket.Repository.PlayerPerformanceSoccerRepository
import com.worldcup.bracket.Repository.PlayerSeasonRepository

import com.worldcup.bracket.Entity.Game
import com.worldcup.bracket.Entity.Team
import com.worldcup.bracket.Entity.Player
import com.worldcup.bracket.Entity.PlayerPerformanceSoccer
import com.worldcup.bracket.Entity.PlayerSeason

import com.worldcup.bracket.Service.BuildNewRequest
import com.worldcup.bracket.FootballAPIData

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.util.Comparator

@Service
class GameService(private val gameRepository : GameRepository,
    private val teamSeasonRepository : TeamSeasonRepository,
    private val leagueRepository : LeagueRepository,
    private val playerPerformanceSoccerRepository : PlayerPerformanceSoccerRepository,
    private val playerSeasonRepository : PlayerSeasonRepository,
    private val footballAPIData : FootballAPIData) {

    private val logger : Logger = LoggerFactory.getLogger(javaClass)

    val httpClient = HttpClient.newHttpClient()

    companion object { 
        const val GAME_POSTPONED = "PST"
    } 

    public fun setLeagueGames(leagueId : String, season : Int) {
        val allFixturesInLeague = gameRepository.getAllGamesInLeagueForSeason(leagueId, season)
        val allTeamsInLeague = teamSeasonRepository.findAllTeamSeasonsBySeasonAndLeague(season, leagueId)
        val relevantLeagueFromDB = leagueRepository.findByIdOrNull(leagueId)!!

        if (allFixturesInLeague.size == 0) { // first time setting fixtures for the season, need to get all fixtures including past fixtures
            
            val allFixturesRequest = BuildNewRequest(footballAPIData.getAllFixturesInSeasonEndpoint(leagueId,season),"GET",null,"x-rapidapi-host",footballAPIData.X_RAPID_API_HOST,"x-rapidapi-key",footballAPIData.FOOTBALL_API_KEY)
            val allFixturesResponse = httpClient.send(allFixturesRequest, HttpResponse.BodyHandlers.ofString());
            val allFixturesResponseWrapper = Gson().fromJson(allFixturesResponse.body(), FixturesAPIResponseWrapper::class.java)
            
            
            val gamesForSeason = mutableListOf<Game>()
            for (game in allFixturesResponseWrapper.response) {
                val relevantHomeTeam = allTeamsInLeague.filter{it.team == game.teams.home}[0]
                val relevantAwayTeam = allTeamsInLeague.filter{it.team == game.teams.away}[0]
                gamesForSeason.add(Game(
                    home=relevantHomeTeam,
                    away=relevantAwayTeam,
                    knockoutGame=false,
                    date=game.fixture.timestamp,
                    fixtureId=game.fixture.id,
                    league=relevantLeagueFromDB
                ))
            }
            gameRepository.saveAll(gamesForSeason)
        } else { // already got past fixtures, this is just to check if upcoming fixtures have had changes in their schedules such as being postponed
            val allFixturesRestOfSeason = BuildNewRequest(footballAPIData.getAllUpcomingFixturesInSeasonEndpoint(leagueId,season),"GET",null,"x-rapidapi-host",footballAPIData.X_RAPID_API_HOST,"x-rapidapi-key",footballAPIData.FOOTBALL_API_KEY)
            val allFixturesRestOfSeasonResponse = httpClient.send(allFixturesRestOfSeason, HttpResponse.BodyHandlers.ofString());
            val allFixturesRestOfSeasonResponseWrapper = Gson().fromJson(allFixturesRestOfSeasonResponse.body(), FixturesAPIResponseWrapper::class.java)
            
            val postponedGames = mutableListOf<Game>()
            val newlyAddedGames = mutableListOf<Game>()
            for (game in allFixturesRestOfSeasonResponseWrapper.response) { // check if any upcoming games have schedule changes, if so update in DB
                val relevantHomeTeam = allTeamsInLeague.filter{it.team == game.teams.home}[0]
                val relevantAwayTeam = allTeamsInLeague.filter{it.team == game.teams.away}[0]
                val relevantGame = Game(
                    home=relevantHomeTeam,
                    away=relevantAwayTeam,
                    knockoutGame=false,
                    date=game.fixture.timestamp,
                    fixtureId=game.fixture.id,
                    league=relevantLeagueFromDB)
                if (game.fixture.status.short == GameService.GAME_POSTPONED) {
                    postponedGames.add(relevantGame)
                }
                else if (! allFixturesInLeague.contains(relevantGame)) {
                    newlyAddedGames.add(relevantGame)
                }
            }
            gameRepository.saveAll(newlyAddedGames)
            gameRepository.deleteAll(postponedGames)
        }
    }

    public fun updateScores(fixtureId : String) {
        val request = BuildNewRequest(footballAPIData.getSingleFixtureEndpoint(fixtureId),"GET",null,"x-rapidapi-host",footballAPIData.X_RAPID_API_HOST,"x-rapidapi-key",footballAPIData.FOOTBALL_API_KEY)
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
                teamSeasonRepository.saveAll(listOf(game.home,game.away))
        }
    }

    private fun setPlayerStatistics(allPlayersBothTeams: List<PlayersNested>, allEvents: List<AllEvents>, relatedGame: Game) {
        logger.info("setting stats")
        val homeTeamId = relatedGame.home.id
        val awayTeamId = relatedGame.away.id
        val allPlayerSeasonsSameGameFromRepo = playerSeasonRepository.findAllPlayersFromOneGame(homeTeamId.toString(),awayTeamId.toString())

        // we will create a new player performance the first time entering this method, otherwise will use existing records
        // this is why it is declared as a mutable list even though when retrieved from the database it will not be used as a mutable list,
        // only used as mutable list the first time when we are creating player performances
        val playerPerformances : MutableList<PlayerPerformanceSoccer> = playerPerformanceSoccerRepository.findAllPlayerPerformancesByGame(relatedGame.fixtureId).toMutableList()
        val firstTimeCreatingPerformances = playerPerformances.size == 0
        

        allPlayersBothTeams.forEach{playerFromAPI ->
            val playerFromRepo = allPlayerSeasonsSameGameFromRepo.filter{playerFromRepo -> playerFromRepo.player.id == playerFromAPI.player.id}[0]
            if (firstTimeCreatingPerformances) {
                playerPerformances.add(
                    PlayerPerformanceSoccer(
                        playerSeason=playerFromRepo,
                        game = relatedGame,
                        minutes = playerFromAPI.statistics[0].games.minutes,
                        started = ! (playerFromAPI.statistics[0].games.substitute),
                        goals = playerFromAPI.statistics[0].goals.total,
                        assists = playerFromAPI.statistics[0].goals.assists,
                        yellowCards = playerFromAPI.statistics[0].cards.yellow,
                        redCards = playerFromAPI.statistics[0].cards.red,
                        saves = playerFromAPI.statistics[0].goals.saves,
                        cleanSheet = if (playerFromRepo.teamSeason.team == relatedGame.home.team) relatedGame.awayScore == 0 else relatedGame.homeScore == 0,
                        penaltySaves = playerFromAPI.statistics[0].penalty.saved,
                        penaltyMisses = playerFromAPI.statistics[0].penalty.missed
                    )
                )
            } else {
                val relevantPlayerPerformance = playerPerformances.filter{pp -> pp.playerSeason.player.id == playerFromAPI.player.id}[0];
                relevantPlayerPerformance.minutes = playerFromAPI.statistics[0].games.minutes
                relevantPlayerPerformance.goals = playerFromAPI.statistics[0].goals.total
                relevantPlayerPerformance.assists = playerFromAPI.statistics[0].goals.assists
                relevantPlayerPerformance.yellowCards = playerFromAPI.statistics[0].cards.yellow
                relevantPlayerPerformance.redCards = playerFromAPI.statistics[0].cards.red
                relevantPlayerPerformance.saves = playerFromAPI.statistics[0].goals.saves
                relevantPlayerPerformance.cleanSheet = if (playerFromRepo.teamSeason.team == relatedGame.home.team) relatedGame.awayScore == 0 else relatedGame.homeScore == 0
                relevantPlayerPerformance.penaltySaves = playerFromAPI.statistics[0].penalty.saved
                relevantPlayerPerformance.penaltyMisses = playerFromAPI.statistics[0].penalty.missed
            }
        }

        allEvents
            .forEach{event ->
                // the api marks own goals as an event but not under each player as an individual statistic.
                // thus we must filter to find the event and add it to the corresponding player 
                if (event.detail == "Own Goal") {
                    val allRelevantPerformances = playerPerformances.filter{pp -> pp.playerSeason.player.id == event.player.id}
                    allRelevantPerformances[0].ownGoals ++
                }
            }

        playerPerformanceSoccerRepository.saveAll(playerPerformances)

        if (relatedGame.scoresAlreadySet) { // indicates that the came is over and we can increment cummulative player stats
            val playersToUpdate = mutableListOf<PlayerSeason>()
            playerPerformances.forEach{
                pp -> 
                var markedForUpdate = false
                if (pp.goals != null && pp.goals!! > 0) {
                    pp.playerSeason.goals += pp.goals!!
                    if (! markedForUpdate) {
                        playersToUpdate.add(pp.playerSeason)
                        markedForUpdate = true
                    }
                }
                if (pp.assists != null && pp.assists!! > 0) {
                    pp.playerSeason.assists += pp.assists!!
                    if (! markedForUpdate) {
                        playersToUpdate.add(pp.playerSeason)
                        markedForUpdate = true
                    }
                }
            }

            // more cummulative updates here

            playerSeasonRepository.saveAll(playersToUpdate)
        }

    }
}