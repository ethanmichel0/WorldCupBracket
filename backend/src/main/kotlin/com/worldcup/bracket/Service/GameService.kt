package com.worldcup.bracket.Service 

import org.springframework.stereotype.Service
import org.springframework.data.repository.findByIdOrNull

import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration
import java.time.Instant
import java.util.GregorianCalendar
import java.util.Comparator

import com.google.gson.Gson; 
import com.google.gson.GsonBuilder;

import com.worldcup.bracket.DTO.FixturesAPIResponseWrapper
import com.worldcup.bracket.DTO.AllEvents
import com.worldcup.bracket.DTO.PlayersNested
import com.worldcup.bracket.DTO.PlayerInfoSinglePlayerRequest

import com.worldcup.bracket.Repository.GameRepository
import com.worldcup.bracket.Repository.TeamSeasonRepository
import com.worldcup.bracket.Repository.LeagueRepository
import com.worldcup.bracket.Repository.PlayerRepository
import com.worldcup.bracket.Repository.PlayerPerformanceSoccerRepository
import com.worldcup.bracket.Repository.PlayerSeasonRepository
import com.worldcup.bracket.Repository.ScheduledTaskRepository

import com.worldcup.bracket.Entity.Game
import com.worldcup.bracket.Entity.Team
import com.worldcup.bracket.Entity.TeamSeason
import com.worldcup.bracket.Entity.Player
import com.worldcup.bracket.Entity.PlayerPerformanceSoccer
import com.worldcup.bracket.Entity.PlayerSeason
import com.worldcup.bracket.Entity.TaskType
import com.worldcup.bracket.Entity.ScheduledTask

import com.worldcup.bracket.Service.BuildNewRequest
import com.worldcup.bracket.GetFootballDataEndpoints

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.jsoup.Jsoup

@Service
class GameService(private val gameRepository : GameRepository,
    private val teamSeasonRepository : TeamSeasonRepository,
    private val leagueRepository : LeagueRepository,
    private val playerPerformanceSoccerRepository : PlayerPerformanceSoccerRepository,
    private val playerSeasonRepository : PlayerSeasonRepository,
    private val playerRepository : PlayerRepository,
    private val scheduledTaskRepository : ScheduledTaskRepository,
    private val schedulerService : SchedulerService,
    private val footballAPIData : GetFootballDataEndpoints) {

    private val logger : Logger = LoggerFactory.getLogger(javaClass)

    val httpClient = HttpClient.newHttpClient()

    public fun setLeagueGames(leagueId : String, season : Int) {
        val allFixturesInLeague = gameRepository.getAllGamesInLeagueForSeason(leagueId, season)
        val allTeamsInLeague = teamSeasonRepository.findAllTeamSeasonsBySeasonAndLeague(season, leagueId)
        val relevantLeagueFromDB = leagueRepository.findByIdOrNull(leagueId)!!

        if (allFixturesInLeague.size == 0) { // first time setting fixtures for the season, need to get all fixtures including past fixtures
            
            
            // we need to use WhoScored for certain stastistics such as errors leading to goal, which unfortunately doesn't have an API. 
            // this means that for each team we must get it's list of fixtures and associated gameId so that we can map games from api football (and in our database) to 
            // games on WhoScored.
            // this function will parse through the html (no api available) from WhoScored and map each game in terms of home and away teams to their match id 
            // so we can later set the WhoScoredId after getting the game from api football to associate these games
            
            val whoScoredMatchIdByHomeTeamAndAwayTeam: MutableMap<Pair<String,String>, String> = HashMap()

            for (team in allTeamsInLeague) {
                val allFixturesWhoScoredForTeamRequest = BuildNewRequest(footballAPIData.getAllFixturesForTeamWhoScored(team.team.teamIdWhoScored!!),"GET")
                // note that we are manually setting teamIdWhoScored after adding a league. If we have not completed this step, we will get an exception from the line above.

                val allFixturesWhoScoredForTeamResponse = httpClient.send(allFixturesWhoScoredForTeamRequest, HttpResponse.BodyHandlers.ofString())

                Jsoup.parseBodyFragment(allFixturesWhoScoredForTeamResponse.body()).body().select("div#layout-wrapper > script")
                    .forEach{
                        it.data().substringAfter("fixtureMatches: [").split(",[")
                        .forEach{
                            val matchDataJavascriptSplitByComma = it.split(",")
                            // there may be multiple script elements that are children of div#layoutWrapper so need to make sure we are in right one
                            if (matchDataJavascriptSplitByComma.size >= 9) {
                                whoScoredMatchIdByHomeTeamAndAwayTeam[Pair(matchDataJavascriptSplitByComma[5].substring(1,matchDataJavascriptSplitByComma[5].lastIndex),matchDataJavascriptSplitByComma[8].substring(1,matchDataJavascriptSplitByComma[8].lastIndex))] = matchDataJavascriptSplitByComma[0]
                            }
                        }
                    }
            }
            
            val allFixturesRequest = BuildNewRequest(footballAPIData.getAllFixturesInSeasonEndpoint(leagueId,season),"GET",null,"x-rapidapi-host",footballAPIData.X_RAPID_API_HOST,"x-rapidapi-key",footballAPIData.FOOTBALL_API_KEY)
            val allFixturesResponse = httpClient.send(allFixturesRequest, HttpResponse.BodyHandlers.ofString());
            val allFixturesResponseWrapper = Gson().fromJson(allFixturesResponse.body(), FixturesAPIResponseWrapper::class.java)
            
            val gamesForSeason = mutableListOf<Game>()
            val allScheduledFixtureRetrievalTasks = mutableListOf<ScheduledTask>()
            
            val currentHomeTeam: TeamSeason? = null

            for (game in allFixturesResponseWrapper.response) {
                val relevantHomeTeam = allTeamsInLeague.filter{it.team == game.teams.home}[0]
                val relevantAwayTeam = allTeamsInLeague.filter{it.team == game.teams.away}[0]
                gamesForSeason.add(Game(
                    home=relevantHomeTeam,
                    away=relevantAwayTeam,
                    knockoutGame=false,
                    date=game.fixture.timestamp,
                    fixtureId=game.fixture.id,
                    league=relevantLeagueFromDB,
                    gameIdWhoScored=whoScoredMatchIdByHomeTeamAndAwayTeam[Pair(relevantHomeTeam.team.name,relevantAwayTeam.team.name)]
                ))

                val cal = GregorianCalendar();
                // reset hour, minutes, seconds and millis
                cal.setTimeInMillis(game.fixture.timestamp.toLong() * 1000)

                allScheduledFixtureRetrievalTasks.add(schedulerService.addNewTask(
                    task = Runnable {updateScores(game.fixture.id)},
                    startTime = cal.toInstant(),
                    repeatEvery = null,
                    type = TaskType.GetScoresForFixture,
                    relatedEntity = game.fixture.id
                ))
            }
            gameRepository.saveAll(gamesForSeason)
            scheduledTaskRepository.saveAll(allScheduledFixtureRetrievalTasks)
        } else { // already got past fixtures, this is just to check if upcoming fixtures have had changes in their schedules such as being postponed
            val allFixturesRestOfSeason = BuildNewRequest(footballAPIData.getAllUpcomingFixturesInSeasonEndpoint(leagueId,season),"GET",null,"x-rapidapi-host",footballAPIData.X_RAPID_API_HOST,"x-rapidapi-key",footballAPIData.FOOTBALL_API_KEY)
            val allFixturesRestOfSeasonResponse = httpClient.send(allFixturesRestOfSeason, HttpResponse.BodyHandlers.ofString());
            val allFixturesRestOfSeasonResponseWrapper = Gson().fromJson(allFixturesRestOfSeasonResponse.body(), FixturesAPIResponseWrapper::class.java)
            
            val postponedGames = mutableListOf<Game>()
            val newlyAddedGames = mutableListOf<Game>()
            val allScheduledFixtureRetrievalTasks = mutableListOf<ScheduledTask>()

            val allRelatedFixtureIdsPostponedGames = mutableListOf<String>()
            for (game in allFixturesRestOfSeasonResponseWrapper.response.sortedWith(compareBy{it.teams.home.name})) { // check if any upcoming games have schedule changes, if so update in DB
                val relevantHomeTeam = allTeamsInLeague.filter{it.team == game.teams.home}[0]
                val relevantAwayTeam = allTeamsInLeague.filter{it.team == game.teams.away}[0]
                val relevantGame = Game(
                    home=relevantHomeTeam,
                    away=relevantAwayTeam,
                    knockoutGame=false,
                    date=game.fixture.timestamp,
                    fixtureId=game.fixture.id,
                    league=relevantLeagueFromDB)

                if (game.fixture.status.short == footballAPIData.STATUS_POSTPONED_SHORT) {
                    postponedGames.add(relevantGame)
                    allRelatedFixtureIdsPostponedGames.add(game.fixture.id) // for later: delete scheduled tasks from DB
                }
                else if (! allFixturesInLeague.contains(relevantGame)) {
                    newlyAddedGames.add(relevantGame)
                    val cal = GregorianCalendar();
                    cal.setTimeInMillis(game.fixture.timestamp.toLong() * 1000)

                    allScheduledFixtureRetrievalTasks.add(schedulerService.addNewTask(
                        task = Runnable {updateScores(game.fixture.id)},
                        startTime = cal.toInstant(),
                        repeatEvery = null,
                        type = TaskType.GetScoresForFixture,
                        relatedEntity = game.fixture.id
                    ))
                }
            }
            gameRepository.saveAll(newlyAddedGames)
            gameRepository.deleteAll(postponedGames)

            scheduledTaskRepository.saveAll(allScheduledFixtureRetrievalTasks)

            // first remove tasks to get postpone games updates from scheduler
            val allRelatedPostponedGames = scheduledTaskRepository.findByRelatedEntityIn(allRelatedFixtureIdsPostponedGames)
            allRelatedPostponedGames.forEach{postponedGame -> schedulerService.removeTaskFromScheduler(postponedGame.id.toString())}
            
            // then remove them from the DB
            scheduledTaskRepository.deleteAll(allRelatedPostponedGames)
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
                if (responseWrapper.response[0].fixture.status.short == footballAPIData.STATUS_FINISHED_SHORT && ! game.scoresAlreadySet) {
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
                } else if (! listOf(footballAPIData.STATUS_POSTPONED_SHORT,footballAPIData.STATUS_SUSPENDED_SHORT,footballAPIData.STATUS_ABANDONED_SHORT).contains(responseWrapper.response[0].fixture.status.short)) {
                    // assuming game is still in progress, schedule next retrieval for a minute in the future (games update every minute)
                    scheduledTaskRepository.save(schedulerService.addNewTask(
                        task = Runnable {updateScores(game.fixtureId)},
                        startTime = Instant.now().plusSeconds(60),
                        repeatEvery = null,
                        type = TaskType.GetScoresForFixture,
                        relatedEntity = game.fixtureId
                    ))
                } // TODO if game is abandoned, notify players
                gameRepository.save(game)
                teamSeasonRepository.saveAll(listOf(game.home,game.away))
        }
    }

    private fun setPlayerStatistics(allPlayersBothTeams: List<PlayersNested>, allEvents: List<AllEvents>, relatedGame: Game) {
        val homeTeamId = relatedGame.home.id
        val awayTeamId = relatedGame.away.id
        val allPlayerSeasonsSameGameFromRepo = playerSeasonRepository.findAllPlayersFromOneGame(homeTeamId.toString(),awayTeamId.toString())

        // we will create a new player performance the first time entering this method, otherwise will use existing records
        // this is why it is declared as a mutable list even though when retrieved from the database it will not be used as a mutable list,
        // only used as mutable list the first time when we are creating player performances
        val playerPerformances : MutableList<PlayerPerformanceSoccer> = playerPerformanceSoccerRepository.findAllPlayerPerformancesByGame(relatedGame.fixtureId).toMutableList()
        val firstTimeCreatingPerformances = playerPerformances.size == 0
        

        allPlayersBothTeams.forEach{playerFromAPI ->
            val matchingPlayerFromRepo = allPlayerSeasonsSameGameFromRepo.filter{playerFromRepo -> playerFromRepo.player.id == playerFromAPI.player.id}.toMutableList()
            if (matchingPlayerFromRepo.size==0) {
                // this indicates that we are retrieving a game earlier this season with players listed who no longer play for the club and hence are 
                // not in the database. This should only happen if we get current sqauds from a league partway through the season when some of the original players have left the club
                // and thus never had PlayerSeason objects created. NOTE that this condition will only enter if we have added a new league partway through it's season

                val playerInfoRequest = BuildNewRequest(footballAPIData.getIndividualPlayerForSeasonEndpoint(playerFromAPI.player.id,relatedGame.home.season),"GET",null,"x-rapidapi-host",footballAPIData.X_RAPID_API_HOST,"x-rapidapi-key",footballAPIData.FOOTBALL_API_KEY)
                val playerInfoResponse = httpClient.send(playerInfoRequest, HttpResponse.BodyHandlers.ofString());
                val playerInfoWrapper : PlayerInfoSinglePlayerRequest = Gson().fromJson(playerInfoResponse.body(), PlayerInfoSinglePlayerRequest::class.java)
                
                val playerWhoLeftClub = if (playerRepository.findByIdOrNull(playerFromAPI.player.id) != null) playerRepository.findByIdOrNull(playerFromAPI.player.id)!! else
                Player(
                    id = playerFromAPI.player.id,
                    name = playerFromAPI.player.name
                )

                // if player is in database (they started season in same league but @ different club (for example Chelsea to Arsenal) than they will already be in database. 
                // however if they joined the league from a team in a different league, (example MLS to Premier League) they may not be in database if the other league is not accounted for)

                val playerSeasonNoLongerPlaysForClub = PlayerSeason(
                    player = playerWhoLeftClub,
                    teamSeason = if (relatedGame.home.team.id == playerInfoWrapper.response[0].statistics[1].team.id) relatedGame.home else relatedGame.away,
                    position = playerInfoWrapper.response[0].statistics[1].games.position,
                    number = playerFromAPI.statistics[0].games.number!!,
                    playerLeftClubDuringSeason = true // this condition will only enter (no player found in database) if we added a league midseason and got an old game with a player who left the club
                    // before we added the league
                )

                matchingPlayerFromRepo.add(playerSeasonNoLongerPlaysForClub)
                playerRepository.save(playerWhoLeftClub)
                playerSeasonRepository.save(playerSeasonNoLongerPlaysForClub)

                logger.info("New player : ${playerWhoLeftClub} added to database since league: ${relatedGame.home.league} was added midseason after player already left club")
            }
            if (firstTimeCreatingPerformances) {
                playerPerformances.add(
                    PlayerPerformanceSoccer(
                        playerSeason=matchingPlayerFromRepo[0],
                        game = relatedGame,
                        minutes = playerFromAPI.statistics[0].games.minutes,
                        started = ! (playerFromAPI.statistics[0].games.substitute),
                        goals = playerFromAPI.statistics[0].goals.total,
                        assists = playerFromAPI.statistics[0].goals.assists,
                        saves = playerFromAPI.statistics[0].goals.saves,
                        totalPasses = playerFromAPI.statistics[0].passes.total,
                        keyPasses = playerFromAPI.statistics[0].passes.key,
                        accuratePasses = playerFromAPI.statistics[0].passes.accuracy,
                        tackles = playerFromAPI.statistics[0].tackles.total,
                        blocks = playerFromAPI.statistics[0].tackles.blocks,
                        interceptions = playerFromAPI.statistics[0].tackles.interceptions,
                        totalDuels = playerFromAPI.statistics[0].duels.total,
                        duelsWon = playerFromAPI.statistics[0].duels.won,
                        dribblesAttempted = playerFromAPI.statistics[0].dribbles.attempts,
                        dribblesSuccesful = playerFromAPI.statistics[0].dribbles.success,
                        dribblesPast = playerFromAPI.statistics[0].dribbles.past,
                        foulsDrawn = playerFromAPI.statistics[0].fouls.drawn,
                        foulsCommitted = playerFromAPI.statistics[0].fouls.committed,
                        yellowCards = playerFromAPI.statistics[0].cards.yellow,
                        redCards = playerFromAPI.statistics[0].cards.red,
                        penaltiesDrawn = playerFromAPI.statistics[0].penalty.won,
                        penaltiesCommitted = playerFromAPI.statistics[0].penalty.committed,
                        penaltiesMissed = playerFromAPI.statistics[0].penalty.missed,
                        penaltiesSaved = playerFromAPI.statistics[0].penalty.saved,
                        penaltiesScored = playerFromAPI.statistics[0].penalty.scored,
                        cleanSheet = if (matchingPlayerFromRepo[0].teamSeason.team == relatedGame.home.team) relatedGame.awayScore == 0 else relatedGame.homeScore == 0,
                    )
                )
            } else {
                val relevantPlayerPerformance = playerPerformances.filter{pp -> pp.playerSeason.player.id == playerFromAPI.player.id}[0];
                relevantPlayerPerformance.minutes = playerFromAPI.statistics[0].games.minutes
                relevantPlayerPerformance.goals = playerFromAPI.statistics[0].goals.total
                relevantPlayerPerformance.assists = playerFromAPI.statistics[0].goals.assists
                relevantPlayerPerformance.saves = playerFromAPI.statistics[0].goals.saves
                relevantPlayerPerformance.totalPasses = playerFromAPI.statistics[0].passes.total
                relevantPlayerPerformance.keyPasses = playerFromAPI.statistics[0].passes.key
                relevantPlayerPerformance.accuratePasses = playerFromAPI.statistics[0].passes.accuracy
                relevantPlayerPerformance.tackles = playerFromAPI.statistics[0].tackles.total
                relevantPlayerPerformance.blocks = playerFromAPI.statistics[0].tackles.blocks
                relevantPlayerPerformance.interceptions = playerFromAPI.statistics[0].tackles.interceptions
                relevantPlayerPerformance.totalDuels = playerFromAPI.statistics[0].duels.total
                relevantPlayerPerformance.duelsWon = playerFromAPI.statistics[0].duels.won
                relevantPlayerPerformance.dribblesAttempted = playerFromAPI.statistics[0].dribbles.attempts
                relevantPlayerPerformance.dribblesSuccesful = playerFromAPI.statistics[0].dribbles.success
                relevantPlayerPerformance.dribblesPast = playerFromAPI.statistics[0].dribbles.past
                relevantPlayerPerformance.foulsDrawn = playerFromAPI.statistics[0].fouls.drawn
                relevantPlayerPerformance.foulsCommitted = playerFromAPI.statistics[0].fouls.committed
                relevantPlayerPerformance.yellowCards = playerFromAPI.statistics[0].cards.yellow
                relevantPlayerPerformance.redCards = playerFromAPI.statistics[0].cards.red
                relevantPlayerPerformance.penaltiesDrawn = playerFromAPI.statistics[0].penalty.won
                relevantPlayerPerformance.penaltiesCommitted = playerFromAPI.statistics[0].penalty.committed
                relevantPlayerPerformance.penaltiesMissed = playerFromAPI.statistics[0].penalty.missed
                relevantPlayerPerformance.penaltiesSaved = playerFromAPI.statistics[0].penalty.saved
                relevantPlayerPerformance.cleanSheet = if (matchingPlayerFromRepo[0].teamSeason.team == relatedGame.home.team) relatedGame.awayScore == 0 else relatedGame.homeScore == 0
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