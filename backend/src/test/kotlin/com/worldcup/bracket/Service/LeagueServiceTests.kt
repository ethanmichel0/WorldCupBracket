package com.worldcup.bracket.Service

import com.github.tomakehurst.wiremock.WireMockServer
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.*
import org.junit.jupiter.api.Assertions.*

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.data.mongo.AutoConfigureDataMongo
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ContextConfiguration

import com.worldcup.bracket.DTO.NewLeagueOptions
import com.worldcup.bracket.Entity.Team
import com.worldcup.bracket.Entity.Game
import com.worldcup.bracket.Entity.Sport
import com.worldcup.bracket.Entity.ScheduleType
import com.worldcup.bracket.FootballAPIData
import com.worldcup.bracket.WireMockUtility
import com.worldcup.bracket.Repository.PlayerRepository
import com.worldcup.bracket.Repository.PlayerSeasonRepository
import com.worldcup.bracket.Repository.TeamSeasonRepository
import com.worldcup.bracket.Repository.LeagueRepository
import com.worldcup.bracket.Repository.TeamRepository
import com.worldcup.bracket.Repository.GameRepository
import com.worldcup.bracket.Repository.ScheduledTaskRepository
import com.worldcup.bracket.Repository.PlayerPerformanceSoccerRepository

import org.junit.jupiter.api.TestMethodOrder
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation
import org.junit.jupiter.api.Order;

import org.springframework.data.repository.findByIdOrNull

import java.time.Duration
import java.util.concurrent.TimeUnit


@TestMethodOrder(OrderAnnotation::class)
@ContextConfiguration(initializers = [WireMockUtility::class])
@AutoConfigureDataMongo
@SpringBootTest
@TestInstance(Lifecycle.PER_CLASS)
class LeagueServiceTests {

    @Autowired 
    private lateinit var footballAPIData: FootballAPIData

    @Autowired
    private lateinit var wireMockServer: WireMockServer

    @Autowired 
    private lateinit var leagueService: LeagueService

    @Autowired 
    private lateinit var gameService: GameService

    @Autowired 
    private lateinit var schedulerService: SchedulerService

    @Autowired
    private lateinit var playerSeasonRepository: PlayerSeasonRepository

    @Autowired 
    private lateinit var teamSeasonRepository: TeamSeasonRepository

    @Autowired 
    private lateinit var teamRepository: TeamRepository

    @Autowired 
    private lateinit var leagueRepository: LeagueRepository

    @Autowired
    private lateinit var playerRepository: PlayerRepository

    @Autowired
    private lateinit var gameRepository: GameRepository

    @Autowired
    private lateinit var scheduledTaskRepository: ScheduledTaskRepository

    @Autowired
    private lateinit var playerPerformanceRepository: PlayerPerformanceSoccerRepository

    @Test
    @Order(1)
    fun `Set up new league`() {

        val leagueInfoEndpoint = footballAPIData.getLeagueEndpoint("39")
        val apiResponseLeagueInfoFileName = "2teampremierleagueinfo.json"
        val leagueInfoResponse: String? = this::class.java.classLoader.getResource(apiResponseLeagueInfoFileName)?.readText()

        val leagueStandingsEndpoint = footballAPIData.getStandingsEndpoint("39",2022)
        val apiResponseLeagueStandingsFileName = "2teampremierleaguestandings.json"
        val leagueStandingsResponse: String? = this::class.java.classLoader.getResource(apiResponseLeagueStandingsFileName)?.readText()

        val leicesterSquad = footballAPIData.getAllPlayersOnTeamEndpoint("46")
        val apiResponseLeicesterBeforeFileName = "squads/LeicesterPlayersBeginning.json"
        val leicesterPlayersBeforeResponse: String? = this::class.java.classLoader.getResource(apiResponseLeicesterBeforeFileName)?.readText()

        val bournemouthSquad = footballAPIData.getAllPlayersOnTeamEndpoint("35")
        val apiResponseBournemouthBeforeFileName = "squads/BournemouthPlayersBeginning.json"
        val bournemouthPlayersBeforeResponse: String? = this::class.java.classLoader.getResource(apiResponseBournemouthBeforeFileName)?.readText()
        
        WireMockUtility.stubResponse(leagueInfoEndpoint, leagueInfoResponse!!,wireMockServer)
        WireMockUtility.stubResponse(leagueStandingsEndpoint, leagueStandingsResponse!!,wireMockServer)
        WireMockUtility.stubResponse(leicesterSquad, leicesterPlayersBeforeResponse!!,wireMockServer)
        WireMockUtility.stubResponse(bournemouthSquad, bournemouthPlayersBeforeResponse!!,wireMockServer)

        var allPlayersInLeague2022 = playerSeasonRepository.findAll()
        assertEquals(0,allPlayersInLeague2022.size,"should be 0 players in league")
        
        leagueService.addNewSeasonForLeague("39",NewLeagueOptions(Sport.Soccer,false,ScheduleType.ThroughSpringOfNextYear))

        val bothTeamSeasons = teamSeasonRepository.findAllTeamSeasonsBySeasonAndLeague(2022,"39")
        assertEquals(2,bothTeamSeasons.size,"checking if two TeamSeasons in repository for Leicester and Bournmouth 2022")

        allPlayersInLeague2022 = playerSeasonRepository.findAll()
        assertEquals(62,allPlayersInLeague2022.size,"should be 62 players in league")

        val leicesterTeamSeason = if (bothTeamSeasons[0].team.id=="46") bothTeamSeasons[0] else bothTeamSeasons[1]
        val allPlayersOnLeicester2022 = playerSeasonRepository.findAllPlayerSeasonsByTeamSeason(leicesterTeamSeason.id.toString())
        assertEquals(31,allPlayersOnLeicester2022.size,"there should be 31 players on Leicester's squad in 2022")

        val fredericksBournemouthTeamSeason2022 = playerSeasonRepository.findAllPlayerSeasonsBySeasonAndPlayer(2022,"18815")
        assertEquals(1,fredericksBournemouthTeamSeason2022.size,"player named R. Fredericks with id 18815 is on Bournemouth in 2022")

        var ouattara = playerSeasonRepository.findAllPlayerSeasonsBySeasonAndPlayer(2022,"284797") // arrived during transfer window
        assertEquals(0,ouattara.size,"Ouattara arrived at Bournemouth during transfer window (shouldn't yet be at Bournemouth)")
        
        val scheduledCheckForGameStartTimes = scheduledTaskRepository.findByRelatedEntity("39")[0]
        assertEquals(Duration.ofHours(24),scheduledCheckForGameStartTimes.repeat)
        assertTrue(schedulerService.futures.get(scheduledCheckForGameStartTimes.id.toString())!!.getDelay(TimeUnit.SECONDS)>0)

        // simulate transfer window

        val apiResponseLeicesterAfterFileName = "squads/LeicesterPlayersAfter.json"
        val leicesterPlayersAfterResponse: String? = this::class.java.classLoader.getResource(apiResponseLeicesterAfterFileName)?.readText()

        val apiResponseBournemouthAfterFileName = "squads/BournemouthPlayersAfter.json"
        val bournemouthPlayersAfterResponse: String? = this::class.java.classLoader.getResource(apiResponseBournemouthAfterFileName)?.readText()

        WireMockUtility.stubResponse(leicesterSquad, leicesterPlayersAfterResponse!!,wireMockServer)
        WireMockUtility.stubResponse(bournemouthSquad, bournemouthPlayersAfterResponse!!,wireMockServer)

        leagueService.addNewSeasonForLeague("39",NewLeagueOptions(Sport.Soccer,false,ScheduleType.ThroughSpringOfNextYear))

        ouattara = playerSeasonRepository.findAllPlayerSeasonsBySeasonAndPlayer(2022,"284797") // arrived during transfer window
        assertEquals(1,ouattara.size,"Ouattara arrived at Bournemouth during transfer window")

        val dennis = playerSeasonRepository.findAllPlayerSeasonsBySeasonAndPlayer(2022,"151756") // left during transfer window
        assertEquals(false,dennis[0].current,"Dennis left Bournemouth during transfer window")
    }

    @Test
    @Order(2)
    fun `game postponed mid season`() {
        val allFixturesInLeagueEndpoint = footballAPIData.getAllFixturesInSeasonEndpoint("39",2022)
        val apiResponseAllFixturesInLeagueEndpointFileName = "allFixturesPremierLeague2022.json"
        val allFixturesInLeagueResponse: String? = this::class.java.classLoader.getResource(apiResponseAllFixturesInLeagueEndpointFileName)?.readText()

        val allFixturesInLeagueEndpointUpcoming = footballAPIData.getAllUpcomingFixturesInSeasonEndpoint("39",2022)
        val apiResponseAllFixturesInLeagueEndpointUpcomingFileName = "allFixturesPremierLeagueUpdated.json"
        val allFixturesInLeagueEndpointUpcomingResponse: String? = this::class.java.classLoader.getResource(apiResponseAllFixturesInLeagueEndpointUpcomingFileName)?.readText()

        WireMockUtility.stubResponse(allFixturesInLeagueEndpoint, allFixturesInLeagueResponse!!,wireMockServer)
        WireMockUtility.stubResponse(allFixturesInLeagueEndpointUpcoming, allFixturesInLeagueEndpointUpcomingResponse!!, wireMockServer) 
        // this endpoint only gives upcoming fixtures as opposed to one above which gives all fixtures in the season including the past in ht

        gameService.setLeagueGames("39",2022)

        var allGames = gameRepository.findAll()
        assertEquals(1,allGames.size, "in our mock league (see resources/allFixturesPremierLeague.json) we only mocked one game being played") 
        // since our pretend premier league only has two teams, there are only two fixtures where they play each other twice, each once at home

        var firstGameId = allGames[0].fixtureId
        assertEquals("868240",firstGameId,"one fixture in season with id: ${868240}")

        var allScheduledTasksToGetGameUpdates = scheduledTaskRepository.findByRelatedEntity("868240")
        assertEquals(allScheduledTasksToGetGameUpdates.size,1,"task at beginning of game to receive score updates")
        assertTrue(schedulerService.futures.get(allScheduledTasksToGetGameUpdates[0].id.toString())!= null)

        val numberOfScheduledTasksBeforeGamePostponed = schedulerService.futures.keys.size // check that the number of tasks doesn't change after updating time of a game
        // (there will no longer be a scheduled task to get results for delayed game -1 but +1 for the new scheduled task to get results for make-up game)

        gameService.setLeagueGames("39",2022)
        // this time we are simulating a game being postponed, so that game should be deleted from db with a new one in its place

        // since a game was rescheduled check that the postponed one was deleted from DB and new one was added
        allGames = gameRepository.findAll()
        assertEquals(1,allGames.size,"postponed game should be deleted from db, new game added")
        firstGameId = allGames[0].fixtureId
        assertEquals("86824099",firstGameId,"the game has been postponed and new game created with different id")

        allScheduledTasksToGetGameUpdates = scheduledTaskRepository.findByRelatedEntity("86824099")
        assertEquals(allScheduledTasksToGetGameUpdates.size,1,"tasks at beginning of every game to receive score updates")
        
        
        // make sure a task was created for newly created game that takes place of postponed game
        assertTrue(schedulerService.futures.get(scheduledTaskRepository.findByRelatedEntity("86824099")[0].id.toString()) != null)
        assertEquals(schedulerService.futures.keys.size,numberOfScheduledTasksBeforeGamePostponed,"-1 scheduled task for postponed game, +1 task for make-up game")
    } 

    @Test
    @Order(3)
    fun `get fixture at halftime`() {
        val singleFixtureEndpointUrl = footballAPIData.getSingleFixtureEndpoint("86824099")
        val apiResponseSingleFixtureEndpointFilename = "singleFixtureHalftime.json"
        val singleFixtureAPIResponse: String? = this::class.java.classLoader.getResource(apiResponseSingleFixtureEndpointFilename)?.readText()

        WireMockUtility.stubResponse(singleFixtureEndpointUrl, singleFixtureAPIResponse!!,wireMockServer)
       
        gameService.updateScores("86824099")
        schedulerService.markTaskAsComplete(scheduledTaskRepository.findByRelatedEntity("86824099")[0].id.toString())
        // normally all scheduled tasks are marked as complete after being done, but since we are manually calling gameService.updateScores instead of 
        // scheduling it as a task as is normally done, we should manually mark as complete to imitate how it would work as a scheduled task


        val game = gameRepository.findByIdOrNull("86824099")!!
        val daka = playerPerformanceRepository.findPlayerPerformanceByPlayerAndGame("1098","86824099")[0]
        assertEquals(game.awayScore,1,"Leicester was up 1-0 at halftime")
        assertEquals(game.homeScore,0,"Bournemouth was down 1-0 at halftime")
        assertEquals(daka.goals,1,"Daka scored 1 goal for Leicester")
        assertEquals(daka.minutes,45,"Daka played all 45 minutes")

        assertEquals(2,scheduledTaskRepository.findByRelatedEntity("86824099").size) 

   }

    @Test
    @Order(4)
    fun `get fixture after game finished`() {
        val singleFixtureEndpointUrl = footballAPIData.getSingleFixtureEndpoint("86824099")
        val apiResponseSingleFixtureEndpointFilename = "singleFixtureFulltime.json"
        val singleFixtureAPIResponse: String? = this::class.java.classLoader.getResource(apiResponseSingleFixtureEndpointFilename)?.readText()

        WireMockUtility.stubResponse(singleFixtureEndpointUrl, singleFixtureAPIResponse!!,wireMockServer)
       
        gameService.updateScores("86824099")
        val game = gameRepository.findByIdOrNull("86824099")!!
        val fredericks = playerPerformanceRepository.findPlayerPerformanceByPlayerAndGame("18815","86824099")[0]
        val leicesterTeamSeason = teamSeasonRepository.findTeamSeasonBySeasonAndTeam(2022,"46")[0]
        val bournemouthTeamSeason = teamSeasonRepository.findTeamSeasonBySeasonAndTeam(2022,"35")[0]
        assertEquals(game.awayScore,1,"Leicester lost 2-1")
        assertEquals(game.homeScore,2,"Bournemouth won 2-1")
        assertEquals(fredericks.ownGoals,1,"Fredericks scored an own goal")
        assertEquals(fredericks.minutes,72,"Fredericks played 72 minutes")
        assertEquals(fredericks.yellowCards,1,"Fredericks got a yellow card")
        assertEquals(leicesterTeamSeason.wins,0,"Leicester lost")
        assertEquals(leicesterTeamSeason.losses,1,"Leicester lost")
        assertEquals(bournemouthTeamSeason.wins,1,"Bournemouth won")
        assertEquals(bournemouthTeamSeason.losses,0,"Bournemouth won")
    }
        
    @AfterAll
    fun `delete data before running next test`() {
        playerRepository.deleteAll()
        playerSeasonRepository.deleteAll()
        teamSeasonRepository.deleteAll()
        teamRepository.deleteAll()
        scheduledTaskRepository.deleteAll()
        leagueRepository.deleteAll()
        gameRepository.deleteAll()
    }
}


