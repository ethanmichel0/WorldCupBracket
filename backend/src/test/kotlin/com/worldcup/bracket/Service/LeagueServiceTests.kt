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
import org.springframework.http.MediaType

import com.worldcup.bracket.DTO.NewLeagueOptions
import com.worldcup.bracket.DTO.NewDraftGroup
import com.worldcup.bracket.Entity.Team
import com.worldcup.bracket.Entity.TeamSeason
import com.worldcup.bracket.Entity.Game
import com.worldcup.bracket.Entity.Sport
import com.worldcup.bracket.Entity.User
import com.worldcup.bracket.Entity.AuthService
import com.worldcup.bracket.Entity.DraftGroup
import com.worldcup.bracket.Entity.PlayerDraft
import com.worldcup.bracket.Entity.ScheduleType
import com.worldcup.bracket.GetFootballDataEndpoints
import com.worldcup.bracket.WireMockUtility
import com.worldcup.bracket.Repository.DraftGroupRepository
import com.worldcup.bracket.Repository.PlayerDraftRepository
import com.worldcup.bracket.Repository.PlayerRepository
import com.worldcup.bracket.Repository.PlayerSeasonRepository
import com.worldcup.bracket.Repository.TeamSeasonRepository
import com.worldcup.bracket.Repository.LeagueRepository
import com.worldcup.bracket.Repository.TeamRepository
import com.worldcup.bracket.Repository.GameRepository
import com.worldcup.bracket.Repository.ScheduledTaskRepository
import com.worldcup.bracket.Repository.UserRepository
import com.worldcup.bracket.Repository.PlayerPerformanceSoccerRepository

import com.google.gson.Gson

import org.junit.jupiter.api.TestMethodOrder
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation
import org.junit.jupiter.api.Order;

import org.springframework.data.repository.findByIdOrNull
import org.springframework.mock.web.MockHttpServletResponse
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders

import java.security.Principal
import javax.security.auth.Subject
import java.time.Duration
import java.util.concurrent.TimeUnit


@TestMethodOrder(OrderAnnotation::class)
@ContextConfiguration(initializers = [WireMockUtility::class])
@AutoConfigureDataMongo
@SpringBootTest
@TestInstance(Lifecycle.PER_CLASS)
class LeagueServiceTests {

    @Autowired 
    private lateinit var footballAPIData: GetFootballDataEndpoints

    @Autowired
    private lateinit var wireMockServer: WireMockServer

    @Autowired 
    private lateinit var leagueService: LeagueService

    @Autowired 
    private lateinit var gameService: GameService

    @Autowired 
    private lateinit var draftGroupService: DraftGroupService

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
    private lateinit var userRepository: UserRepository

    @Autowired
    private lateinit var draftGroupRepository: DraftGroupRepository

    @Autowired
    private lateinit var playerPerformanceRepository: PlayerPerformanceSoccerRepository

    @Autowired
    private lateinit var playerDraftRepository: PlayerDraftRepository

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
        
        leagueService.addNewSeasonForLeague("39",NewLeagueOptions(Sport.Soccer,false,ScheduleType.ThroughSpringOfNextYear,"2"))
        
        // manually set WhoScored.com ids for both teams as we will do in production
        val bournemouth = teamSeasonRepository.findAllCurrentTeamSeasonsByTeam("35")[0]
        val leicester = teamSeasonRepository.findAllCurrentTeamSeasonsByTeam("46")[0]

        bournemouth.team.teamIdWhoScored="183"
        leicester.team.teamIdWhoScored="14"

        teamRepository.saveAll(mutableListOf<Team>(bournemouth.team,leicester.team))
        teamSeasonRepository.saveAll(mutableListOf<TeamSeason>(bournemouth,leicester))


        val bothTeamSeasons = teamSeasonRepository.findAllTeamSeasonsBySeasonAndLeague(2022,"39")
        assertEquals(2,bothTeamSeasons.size,"checking if two TeamSeasons in repository for Leicester and Bournmouth 2022")

        allPlayersInLeague2022 = playerSeasonRepository.findAll()
        assertEquals(62,allPlayersInLeague2022.size,"should be 62 players in league")

        val leicesterTeamSeason = if (bothTeamSeasons[0].team.id=="46") bothTeamSeasons[0] else bothTeamSeasons[1]
        val allPlayersOnLeicester2022 = playerSeasonRepository.findAllPlayerSeasonsByTeamSeason(leicesterTeamSeason.id.toString())
        assertEquals(31,allPlayersOnLeicester2022.size,"there should be 31 players on Leicester's squad in 2022")

        val fredericksBournemouthTeamSeason2022 = playerSeasonRepository.findPlayerSeasonBySeasonAndPlayer(2022,"18815")
        assertEquals(1,fredericksBournemouthTeamSeason2022.size,"player named R. Fredericks with id 18815 is on Bournemouth in 2022")

        var ouattara = playerSeasonRepository.findPlayerSeasonBySeasonAndPlayer(2022,"284797") // arrived during transfer window
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

        leagueService.addNewSeasonForLeague("39",NewLeagueOptions(Sport.Soccer,false,ScheduleType.ThroughSpringOfNextYear,"2"))

        ouattara = playerSeasonRepository.findPlayerSeasonBySeasonAndPlayer(2022,"284797") // arrived during transfer window
        assertEquals(1,ouattara.size,"Ouattara arrived at Bournemouth during transfer window")

        val dennis = playerSeasonRepository.findPlayerSeasonBySeasonAndPlayer(2022,"151756") // left during transfer window
        assertEquals(true,dennis[0].playerLeftClubDuringSeason,"Dennis left Bournemouth during transfer window")
    }

    @Test
    @Order(2)
    fun `new draft group`() {
        // because drafting has timers involved and websockets, this testing was carried out manually for the most part
        // the part we will integration test is whether players obtain points for the players that they have drafted on their teams
        val bob = User(
            principalId="testpid",
            name="Bob",
            email="bob@gmail.com",
            service=AuthService.GOOGLE
        )
        val billy = User(
            principalId="testpid2",
            name="Billy",
            email="billy@gmail.com",
            service=AuthService.GOOGLE
        )
        userRepository.saveAll(listOf<User>(bob,billy))

        var prem = leagueRepository.findAll()[0]
        leagueRepository.save(prem)
        draftGroupService.saveNewDraftGroup(
            NewDraftGroup(
                name="testDraftGroupzz",
                password="password"
            ),
            BobPrincipal()
        )

        draftGroupService.joinDraftGroup(NewDraftGroup(name="testDraftGroupzz",password="password"),BillyPrincipal())
        val draftGroup = draftGroupRepository.findAll()[0]
        assertEquals(2,draftGroup.members.size,"after billy joined there should be two draft group members")

        // we will simulate a draft without actually calling the draft service, as we don't want to worry about scheduled tasks every
        // minute for auto draft. Note that this functionality was tested manually.
        
        // normally playerDrafts objects are created after the draft time is set, but because we don't want to set draft time since we are avoiding
        // scheduled services, we will manually create them
        playerDraftRepository.saveAll(
            listOf<PlayerDraft>(PlayerDraft(
                userEmail="bob@gmail.com",
                userName="Bob",
                draftGroup=draftGroup
            ),PlayerDraft(
                userEmail="billy@gmail.com",
                userName="Billy",
                draftGroup=draftGroup
            )
        ))
        val bobPlayerDraft = playerDraftRepository.findPlayerDraftByGroupAndUserEmail("testDraftGroupzz","bob@gmail.com")[0]

        bobPlayerDraft.draftedForwards.add(playerSeasonRepository.findPlayerSeasonBySeasonAndPlayer(2022,"1098")[0])
        bobPlayerDraft.draftedForwards.add(playerSeasonRepository.findPlayerSeasonBySeasonAndPlayer(2022,"18883")[0])

        playerDraftRepository.save(bobPlayerDraft)
    } 

    @Test
    @Order(3)
    fun `game postponed mid season`() {
        val allFixturesInLeagueEndpoint = footballAPIData.getAllFixturesInSeasonEndpoint("39",2022)
        val apiResponseAllFixturesInLeagueEndpointFileName = "allFixturesPremierLeague2022.json"
        val allFixturesInLeagueResponse: String? = this::class.java.classLoader.getResource(apiResponseAllFixturesInLeagueEndpointFileName)?.readText()

        val allFixturesInLeagueEndpointUpcoming = footballAPIData.getAllUpcomingFixturesInSeasonEndpoint("39",2022)
        val apiResponseAllFixturesInLeagueEndpointUpcomingFileName = "allFixturesPremierLeagueUpdated.json"
        val allFixturesInLeagueEndpointUpcomingResponse: String? = this::class.java.classLoader.getResource(apiResponseAllFixturesInLeagueEndpointUpcomingFileName)?.readText()

        val bournemouthFixturesWhoScored = footballAPIData.getAllFixturesForTeamWhoScored("183")
        val bournemouthFixturesWhoScoredFileName = "whoscored/bournemouthFixtures2022.html"
        val bournemouthFixturesWhoScoredResponse: String? = this::class.java.classLoader.getResource(bournemouthFixturesWhoScoredFileName)?.readText()

        val leicesterFixturesWhoScored = footballAPIData.getAllFixturesForTeamWhoScored("14")
        val leicesterFixturesWhoScoredFileName = "whoscored/leicesterFixtures2022.html"
        val leicesterFixturesWhoScoredResponse: String? = this::class.java.classLoader.getResource(leicesterFixturesWhoScoredFileName)?.readText()

        WireMockUtility.stubResponse(allFixturesInLeagueEndpoint, allFixturesInLeagueResponse!!,wireMockServer)
        WireMockUtility.stubResponse(allFixturesInLeagueEndpointUpcoming, allFixturesInLeagueEndpointUpcomingResponse!!, wireMockServer) 
        // this endpoint only gives upcoming fixtures as opposed to one above which gives all fixtures in the season including the past

        WireMockUtility.stubResponse(bournemouthFixturesWhoScored, bournemouthFixturesWhoScoredResponse!!,wireMockServer)
        WireMockUtility.stubResponse(leicesterFixturesWhoScored, leicesterFixturesWhoScoredResponse!!,wireMockServer)

        gameService.setLeagueGames("39",2022)

        var allGames = gameRepository.findAll()
        assertEquals(1,allGames.size, "in our mock league (see resources/allFixturesPremierLeague.json) we only mocked one game being played") 
        // since our pretend premier league only has two teams, there are only two fixtures where they play each other twice, each once at home

        var firstGameId = allGames[0].fixtureId
        assertEquals("868240",firstGameId,"one fixture in season with id: ${868240}")
        assertEquals(allGames[0].gameIdWhoScored,"1640765","the corresponding game id on whoscored website is 1640765")

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
    @Order(4)
    fun `get fixture at halftime`() {
        var game = gameRepository.findByIdOrNull("86824099")!!

        game.gameIdWhoScored="1640765" // i don't know how whoscored handles delayed games (if new games are created with new ids or the same game with same ids are kept with different times.
        // this until I have info on this TODO test!!! we must manually set the gameIdWhoScored property)
        gameRepository.save(game)

        val singleFixtureEndpointUrl = footballAPIData.getSingleFixtureEndpoint("86824099")
        val apiResponseSingleFixtureEndpointFilename = "singleFixtureHalftime.json"
        val singleFixtureAPIResponse: String? = this::class.java.classLoader.getResource(apiResponseSingleFixtureEndpointFilename)?.readText()

        val whoscoredFixtureEndpoint = footballAPIData.getIndividualFixtureWhoScored("1640765")
        val whoscoredFixtureResponseFileName = "whoscored/bournemouthvsleicesterfixture.html"
        val whoscoredFixtureResponse: String? = this::class.java.classLoader.getResource(whoscoredFixtureResponseFileName)?.readText()

        WireMockUtility.stubResponse(singleFixtureEndpointUrl, singleFixtureAPIResponse!!,wireMockServer)
        WireMockUtility.stubResponse(whoscoredFixtureEndpoint, whoscoredFixtureResponse!!,wireMockServer)
       
        gameService.updateScores("86824099")
        schedulerService.markTaskAsComplete(scheduledTaskRepository.findByRelatedEntity("86824099")[0].id.toString())
        // normally all scheduled tasks are marked as complete after being done, but since we are manually calling gameService.updateScores instead of 
        // scheduling it as a task as is normally done, we should manually mark as complete to imitate how it would work as a scheduled task
        
        game = gameRepository.findByIdOrNull("86824099")!!
        val daka = playerPerformanceRepository.findPlayerPerformanceByPlayerAndGame("1098","86824099")[0]
        val dakaPlayerSeason = playerSeasonRepository.findPlayerSeasonBySeasonAndPlayer(2022,"1098")[0]
        assertEquals(game.awayScore,1,"Leicester was up 1-0 at halftime")
        assertEquals(game.homeScore,0,"Bournemouth was down 1-0 at halftime")
        assertEquals(daka.goals,1,"Daka scored 1 goal for Leicester")
        assertEquals(daka.minutes,45,"Daka played all 45 minutes")
        assertEquals(daka.penaltiesDrawn,1,"Daka won that pen bruh")
        assertEquals(daka.penaltiesCommitted,1,"Daka committed that pen bruh")
        assertEquals(dakaPlayerSeason.goals,1,"So Far Daka has scored 1 goal during the season")
        assertEquals(2,scheduledTaskRepository.findByRelatedEntity("86824099").size) 
   }

    @Test
    @Order(5)
    fun `get fixture after game finished`() {
        val singleFixtureEndpointUrl = footballAPIData.getSingleFixtureEndpoint("86824099")
        val apiResponseSingleFixtureEndpointFilename = "singleFixtureFulltime.json"
        val singleFixtureAPIResponse: String? = this::class.java.classLoader.getResource(apiResponseSingleFixtureEndpointFilename)?.readText()

        val whoscoredFixtureEndpoint = footballAPIData.getIndividualFixtureWhoScored("1640765")
        val whoscoredFixtureResponseFileName = "whoscored/bournemouthvsleicesterfixture.html"
        val whoscoredFixtureResponse: String? = this::class.java.classLoader.getResource(whoscoredFixtureResponseFileName)?.readText()

        WireMockUtility.stubResponse(whoscoredFixtureEndpoint, whoscoredFixtureResponse!!,wireMockServer)
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
        assertEquals(bournemouthTeamSeason.position,1,"Bouremouth is ahead of leicester since they won when they playeded")
        assertEquals(leicesterTeamSeason.position,2,"Bouremouth is ahead of leicester since they won when they playeded")

    }

    @Test
    @Order(6)
    fun `check points on player drafts`() {
        val bobPlayerDraft = playerDraftRepository.findPlayerDraftByGroupAndUserEmail("testDraftGroupzz","bob@gmail.com")[0]
        // TODO this will change as the points system is updated
        // assertEquals(2,bobPlayerDraft.draftedForwards.size,"Bob drafted 2 forwards")
        //assertEquals(2,bobPlayerDraft.performancesByRound.get("Premier League Regular Season - 30")!!.size,"2 performacnes")
        assertEquals(3,bobPlayerDraft.pointsByRound.get("Premier League Regular Season - 30"),"Bob gets a point for the Daka scoring, and Solanke assisting")
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
        userRepository.deleteAll()
        draftGroupRepository.deleteAll()
        playerDraftRepository.deleteAll()
    }
}

class BillyPrincipal : Principal {
    override fun getName(): String = "testpid2"
    override fun implies(subject: Subject) = true
}

class BobPrincipal : Principal {
    override fun getName(): String = "testpid"
    override fun implies(subject: Subject) = true
}

