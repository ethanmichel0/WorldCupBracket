package com.worldcup.bracket.Service

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.*
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.data.mongo.AutoConfigureDataMongo
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.context.ContextConfiguration

import com.worldcup.bracket.Entity.Team
import com.worldcup.bracket.Entity.Game
import com.worldcup.bracket.FootballAPIData
import com.worldcup.bracket.Repository.GameRepository
import com.worldcup.bracket.Repository.TeamRepository

import org.springframework.data.repository.findByIdOrNull



@ActiveProfiles("test")
@ContextConfiguration(initializers = [WireMockContextInitializer::class])
@AutoConfigureDataMongo
@SpringBootTest
class GameServiceTests {

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

    private fun stubResponse(url: String, responseBody: String, responseStatus: Int = HttpStatus.OK.value()) {
        wireMockServer.stubFor(get(urlEqualTo(url))
            .willReturn(
                aResponse()
                .withStatus(responseStatus)
                .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .withBody(responseBody))
        )
    }

    private val apiResponseFileName = "singleFixtureAPIResponse.json"
    private val singleFixtureResponse: String? = this::class.java.classLoader.getResource(apiResponseFileName)?.readText()

    @Test
    fun `test open weather api response is loaded`(){
        assert(singleFixtureResponse != null)
    }

    @Test
    fun `game not yet completed`() {
        val team1 = Team("Bournemouth","A")
        val team2 = Team("Leicester","B")
        teamRepository.saveAll(listOf(team1, team2))
        team1.id="35"
        team2.id="46"
        val game = Game(team1,team2,"A",false,1665237600,"868036")
        gameRepository.save(game)
        val url = footballAPIData.setSingleFixtureAPI("868036")

        stubResponse("/footballAPI/fixtures?id=868036", singleFixtureResponse!!)
        gameService.updateScores("868036")

        val relevantGame = gameRepository.findByIdOrNull("868036")!!
        assert(relevantGame.currentMinute==45)
    }
}