package com.worldcup.bracket.Service

import com.github.tomakehurst.wiremock.WireMockServer
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
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

import org.springframework.data.repository.findByIdOrNull



@ActiveProfiles("skipDataInitialization")
@ContextConfiguration(initializers = [WireMockUtility::class])
@AutoConfigureDataMongo
@SpringBootTest
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

    @Test
    fun `game not yet completed`() {
        val apiResponseFileName = "singleFixtureHalftime.json"
        val singleFixtureResponse: String? = this::class.java.classLoader.getResource(apiResponseFileName)?.readText()

        val home = Team("Bournemouth","A")
        val away = Team("Leicester","B")
        teamRepository.saveAll(listOf(home, away))
        home.id="35"
        away.id="46"
        val game = Game(home,away,"A",false,1665237600,"868036")
        gameRepository.save(game)
        val url = footballAPIData.setSingleFixtureAPI("868036")

        val urlRelativePath = url.split("/").toTypedArray().copyOfRange(3,5).joinToString("/")
        // this will change http://localhost:PORT/fixtures/fixtures?id=${id} to just /fixtures/fixtures?id=${id}

        WireMockUtility.stubResponse("/"+urlRelativePath, singleFixtureResponse!!,wireMockServer)
        gameService.updateScores("868036")

        val relevantGame = gameRepository.findByIdOrNull("868036")!!
        assert(relevantGame.currentMinute==45)
        assert(relevantGame.homeScore==0)
        assert(relevantGame.awayScore==1)
        assert(relevantGame.away.goalsFor==0) // want to check that away and home goals still aren't counted since game is still ongoing
    }

    @Test
    fun `game completed`() {
        val apiResponseFileName = "singleFixtureFulltime.json"
        val singleFixtureResponse: String? = this::class.java.classLoader.getResource(apiResponseFileName)?.readText()

        val home = Team("Bournemouth","A")
        val away = Team("Leicester","B")
        teamRepository.saveAll(listOf(home, away))
        home.id="35"
        away.id="46"
        val game = Game(home,away,"A",false,1665237600,"868036")
        gameRepository.save(game)
        val url = footballAPIData.setSingleFixtureAPI("868036")

        val urlRelativePath = url.split("/").toTypedArray().copyOfRange(3,5).joinToString("/")
        // this will change http://localhost:PORT/fixtures/fixtures?id=${id} to just /fixtures/fixtures?id=${id}

        WireMockUtility.stubResponse("/"+urlRelativePath, singleFixtureResponse!!,wireMockServer)
        gameService.updateScores("868036")

        val relevantGame = gameRepository.findByIdOrNull("868036")!!
        assert(relevantGame.currentMinute==90)
        assert(relevantGame.homeScore==2)
        assert(relevantGame.awayScore==1)
        assert(relevantGame.winner==home)
        assert(relevantGame.scoresAlreadySet)
        assert(relevantGame.home.goalsForGroup==2) 
        assert(relevantGame.away.goalsForGroup==1) 
        assert(relevantGame.away.goalsAgainstGroup==2) 
        assert(relevantGame.home.goalsAgainstGroup==1) 
        assert(relevantGame.home.winsGroup==1)
        assert(relevantGame.home.lossesGroup==0)
        assert(relevantGame.away.winsGroup==0)
        assert(relevantGame.away.lossesGroup==1)
    }
}