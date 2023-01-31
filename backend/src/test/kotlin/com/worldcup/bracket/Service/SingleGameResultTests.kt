package com.worldcup.bracket.Service

import com.github.tomakehurst.wiremock.WireMockServer
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.*

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.data.mongo.AutoConfigureDataMongo
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration

import com.worldcup.bracket.Entity.Team
import com.worldcup.bracket.Entity.Game
import com.worldcup.bracket.FootballAPIData
import com.worldcup.bracket.WireMockUtility
import com.worldcup.bracket.Repository.GameRepository
import com.worldcup.bracket.Repository.TeamRepository
import com.worldcup.bracket.Repository.PlayerRepository
import com.worldcup.bracket.Repository.PlayerPerformanceSoccerRepository
import com.worldcup.bracket.addPlayersFromTeam

import org.springframework.data.repository.findByIdOrNull



@ActiveProfiles("skipDataInitialization")
@ContextConfiguration(initializers = [WireMockUtility::class])
@AutoConfigureDataMongo
@SpringBootTest
@TestInstance(Lifecycle.PER_CLASS)
class SingleGameResultTests {


    @Autowired
    private lateinit var wireMockServer: WireMockServer

    @Autowired 
    private lateinit var footballAPIData: FootballAPIData

    @Autowired 
    private lateinit var gameService: GameService

    @Autowired
    private lateinit var gameRepository: GameRepository

    @Autowired 
    private lateinit var teamRepository: TeamRepository

    @Autowired
    private lateinit var playerRepository: PlayerRepository

    @Autowired
    private lateinit var playerPerformanceRepository: PlayerPerformanceSoccerRepository

    @BeforeAll 
    fun addPlayersFromBothTeamsAndMock() {

        val home = Team("Bournemouth","A")
        val away = Team("Leicester","B")
        teamRepository.saveAll(listOf(home, away))
        home.id="35"
        away.id="46"
        val game = Game(home,away,"A",false,1665237600,"868036")
        gameRepository.save(game)

        val playersOnBournemouthUrl = footballAPIData.getAllPlayersOnTeam("35")
        val playersOnLeicesterUrl = footballAPIData.getAllPlayersOnTeam("46")

        val playersOnBournemouthUrlRelative = "/" + playersOnBournemouthUrl.split("/").toTypedArray().copyOfRange(3,5).joinToString("/")
        val playersOnLeicesterUrlRelative = "/" + playersOnLeicesterUrl.split("/").toTypedArray().copyOfRange(3,5).joinToString("/")

        val apiResponseBournemouthFileName = "squads/BournemouthPlayers.json"
        val bournemouthPlayersResponse: String? = this::class.java.classLoader.getResource(apiResponseBournemouthFileName)?.readText()
        val apiResponseLeicesterFileName = "squads/LeicesterPlayers.json"
        val leicesterPlayersResponse: String? = this::class.java.classLoader.getResource(apiResponseLeicesterFileName)?.readText()

        WireMockUtility.stubResponse(playersOnBournemouthUrlRelative, bournemouthPlayersResponse!!,wireMockServer)
        WireMockUtility.stubResponse(playersOnLeicesterUrlRelative, leicesterPlayersResponse!!,wireMockServer)

        val playersHome = addPlayersFromTeam(home,footballAPIData)
        val playersAway = addPlayersFromTeam(away,footballAPIData)

        playerRepository.saveAll(playersHome)
        playerRepository.saveAll(playersAway)
    }

    @Test
    fun `game not yet completed`() {


        val fixturesUrl = footballAPIData.setSingleFixtureAPI("868036")
        val fixturesUrlRelativePath = "/"+ fixturesUrl.split("/").toTypedArray().copyOfRange(3,5).joinToString("/")
        // this will change http://localhost:PORT/footballAPI/fixtures?id=${id} to just /fixtures/fixtures?id=${id}

        val apiResponseFixtureFileName = "singleFixtureHalftime.json"
        val singleFixtureResponse: String? = this::class.java.classLoader.getResource(apiResponseFixtureFileName)?.readText()

        WireMockUtility.stubResponse(fixturesUrlRelativePath, singleFixtureResponse!!,wireMockServer)

        gameService.updateScores("868036")

        val relevantGame = gameRepository.findByIdOrNull("868036")!!
        assert(relevantGame.currentMinute==45)
        assert(relevantGame.homeScore==0)
        assert(relevantGame.awayScore==1)
        assert(relevantGame.away.goalsFor==0) // want to check that away and home goals still aren't counted since game is still ongoing
        
        val dakaPerformance = playerPerformanceRepository.findAllPlayerPerformancesByPlayerAndGame(1098,"868036")[0]
        assert(dakaPerformance.goals==1)
        assert(dakaPerformance.minutes==45)
        assert(dakaPerformance.cleanSheet)

        val fredericksPerformance = playerPerformanceRepository.findAllPlayerPerformancesByPlayerAndGame(18815,"868036")[0]
        assert(fredericksPerformance.yellowCards==1)
        assert(fredericksPerformance.minutes==45)

    }

    @Test
    fun `game completed`() {
    
        val fixturesUrl = footballAPIData.setSingleFixtureAPI("868036")
        val fixturesUrlRelativePath = "/"+ fixturesUrl.split("/").toTypedArray().copyOfRange(3,5).joinToString("/")
        // this will change http://localhost:PORT/footballAPI/fixtures?id=${id} to just /fixtures/fixtures?id=${id}

        val apiResponseFixtureFileName = "singleFixtureFulltime.json"
        val singleFixtureResponse: String? = this::class.java.classLoader.getResource(apiResponseFixtureFileName)?.readText()

        WireMockUtility.stubResponse(fixturesUrlRelativePath, singleFixtureResponse!!,wireMockServer)
        
        gameService.updateScores("868036")

        val relevantGame = gameRepository.findByIdOrNull("868036")!!
        assert(relevantGame.currentMinute==90)
        assert(relevantGame.homeScore==2)
        assert(relevantGame.awayScore==1)
        assert(relevantGame.winner!!.name=="Bournemouth")
        assert(relevantGame.scoresAlreadySet)
        assert(relevantGame.home.goalsForGroup==2) 
        assert(relevantGame.away.goalsForGroup==1) 
        assert(relevantGame.away.goalsAgainstGroup==2) 
        assert(relevantGame.home.goalsAgainstGroup==1) 
        assert(relevantGame.home.winsGroup==1)
        assert(relevantGame.home.lossesGroup==0)
        assert(relevantGame.away.winsGroup==0)
        assert(relevantGame.away.lossesGroup==1)

        // NOTE -- I marked an own goal for Fredericks to test functinoality but in reality in the game there was no own goal. If you view the actual game by 
        // sending a request to the api the response will be different w/o an own goal as there is in singleFixtureFulltime.json
        val dakaPerformance = playerPerformanceRepository.findAllPlayerPerformancesByPlayerAndGame(1098,"868036")
        val fredericksPerformance = playerPerformanceRepository.findAllPlayerPerformancesByPlayerAndGame(18815,"868036")
        assert(fredericksPerformance.size==1)
        assert(dakaPerformance.size==1)
        assert(fredericksPerformance[0].ownGoals==1)
        assert(! fredericksPerformance[0].cleanSheet)

    }
}