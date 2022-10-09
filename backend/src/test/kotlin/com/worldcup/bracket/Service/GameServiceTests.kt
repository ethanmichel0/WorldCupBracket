package com.worldcup.bracket.Service

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.*
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.context.ContextConfiguration
import reactor.core.publisher.Mono

import com.worldcup.bracket.Entity.Team
import com.worldcup.bracket.Entity.Game
import com.worldcup.bracket.FootballAPIData

@SpringBootTest
@ActiveProfiles("test")
@ContextConfiguration(initializers = [WireMockContextInitializer::class])
class GameServiceTests {

    @Autowired
    private lateinit var wireMockServer: WireMockServer

    @Autowired 
    lateinit var footballAPIData: FootballAPIData

    private fun stubResponse(url: String, responseBody: String, responseStatus: Int = HttpStatus.OK.value()) {
        wireMockServer.stubFor(get(urlEqualTo(url))
            .willReturn(
                aResponse()
                .withStatus(responseStatus)
                .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .withBody(responseBody))
        )
    }

    private fun createNewFixture() : Game {
        val team1 = Team("Bournemouth","A")
        val team2 = Team("Leicester","B")
        team1.id="35"
        team2.id="46"
        return Game(team1,team2,"A",false,1665237600,"868036")
    }

    private val apiResponseFileName = "singleFixtureAPIResponse.json"
    private val openWeatherApiResponse: String? = this::class.java.classLoader.getResource(apiResponseFileName)?.readText()

    @Test
    fun `test open weather api response is loaded`(){
        assert(openWeatherApiResponse != null)
    }

    @Test
    fun `game not yet completed`() {
        val url = footballAPIData.setSingleFixtureAPI("1")
        print(url + "is url")

        stubResponse(url, openWeatherApiResponse!!)
    }
}