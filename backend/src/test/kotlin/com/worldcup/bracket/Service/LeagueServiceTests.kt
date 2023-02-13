package com.worldcup.bracket.Service

import com.github.tomakehurst.wiremock.WireMockServer
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.BeforeAll
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
import com.worldcup.bracket.FootballAPIData
import com.worldcup.bracket.WireMockUtility
import com.worldcup.bracket.Repository.PlayerSeasonRepository
import com.worldcup.bracket.Repository.TeamSeasonRepository
import com.worldcup.bracket.Repository.PlayerRepository
import com.worldcup.bracket.Repository.GameRepository

import org.springframework.data.repository.findByIdOrNull



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
    private lateinit var playerSeasonRepository: PlayerSeasonRepository

    @Autowired 
    private lateinit var teamSeasonRepository: TeamSeasonRepository

    @Autowired 
    private lateinit var gameRepository: GameRepository

    @Autowired
    private lateinit var playerRepository: PlayerRepository

    @BeforeAll
    @Test
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

        val allFixturesInLeagueEndpoint = footballAPIData.getAllFixturesInSeasonEndpoint("39",2022)
        val apiResponseAllFixturesInLeagueEndpointFileName = "allFixturesPremierLeague2022.json"
        val allFixturesInLeagueResponse: String? = this::class.java.classLoader.getResource(apiResponseAllFixturesInLeagueEndpointFileName)?.readText()

        val allFixturesInLeagueEndpointUpcoming = footballAPIData.getAllUpcomingFixturesInSeasonEndpoint("39",2022)
        val apiResponseAllFixturesInLeagueEndpointUpcomingFileName = "allFixturesPremierLeagueUpdated.json"
        val allFixturesInLeagueEndpointUpcomingResponse: String? = this::class.java.classLoader.getResource(apiResponseAllFixturesInLeagueEndpointUpcomingFileName)?.readText()
        
        WireMockUtility.stubResponse(leagueInfoEndpoint, leagueInfoResponse!!,wireMockServer)
        WireMockUtility.stubResponse(leagueStandingsEndpoint, leagueStandingsResponse!!,wireMockServer)
        WireMockUtility.stubResponse(leicesterSquad, leicesterPlayersBeforeResponse!!,wireMockServer)
        WireMockUtility.stubResponse(bournemouthSquad, bournemouthPlayersBeforeResponse!!,wireMockServer)
        WireMockUtility.stubResponse(allFixturesInLeagueEndpoint, allFixturesInLeagueResponse!!,wireMockServer)
        WireMockUtility.stubResponse(allFixturesInLeagueEndpointUpcoming, allFixturesInLeagueEndpointUpcomingResponse!!, wireMockServer) 
        // this endpoint only gives upcoming fixtures as opposed to one above which gives all fixtures in the season including the past in ht

        leagueService.addNewSeasonForLeague("39",NewLeagueOptions(Sport.Soccer,false))

        val bothTeamSeasons = teamSeasonRepository.findAllTeamSeasonsBySeasonAndLeague(2022,"39")
        assertEquals(2,bothTeamSeasons.size,"checking if two TeamSeasons in repository for Leicester and Bournmouth 2022")

        val allPlayersInLeague2022 = playerSeasonRepository.findAll()
        assertEquals(62,allPlayersInLeague2022.size,"should be 62 players in league")

        val leicesterTeamSeason = if (bothTeamSeasons[0].team.id=="46") bothTeamSeasons[0] else bothTeamSeasons[1]
        val allPlayersOnLeicester2022 = playerSeasonRepository.findAllPlayerSeasonsByTeamSeason(leicesterTeamSeason.id.toString())
        assertEquals(31,allPlayersOnLeicester2022.size,"there should be 31 players on Leicester's squad in 2022")

        val fredericksBournemouthTeamSeason2022 = playerSeasonRepository.findAllPlayerSeasonsBySeasonAndPlayer(2022,"18815")
        assertEquals(1,fredericksBournemouthTeamSeason2022.size,"player named R. Fredericks with id 18815 is on Bournemouth in 2022")

        var ouattara = playerSeasonRepository.findAllPlayerSeasonsBySeasonAndPlayer(2022,"284797") // arrived during transfer window
        assertEquals(0,ouattara.size,"Ouattara arrived at Bournemouth during transfer window (shouldn't yet be at Bournemouth)")
        
        // simulate transfer window

        val apiResponseLeicesterAfterFileName = "squads/LeicesterPlayersAfter.json"
        val leicesterPlayersAfterResponse: String? = this::class.java.classLoader.getResource(apiResponseLeicesterAfterFileName)?.readText()

        val apiResponseBournemouthAfterFileName = "squads/BournemouthPlayersAfter.json"
        val bournemouthPlayersAfterResponse: String? = this::class.java.classLoader.getResource(apiResponseBournemouthAfterFileName)?.readText()

        WireMockUtility.stubResponse(leicesterSquad, leicesterPlayersAfterResponse!!,wireMockServer)
        WireMockUtility.stubResponse(bournemouthSquad, bournemouthPlayersAfterResponse!!,wireMockServer)

        leagueService.addNewSeasonForLeague("39",NewLeagueOptions(Sport.Soccer,false))

        ouattara = playerSeasonRepository.findAllPlayerSeasonsBySeasonAndPlayer(2022,"284797") // arrived during transfer window
        assertEquals(1,ouattara.size,"Ouattara arrived at Bournemouth during transfer window")

        val dennis = playerSeasonRepository.findAllPlayerSeasonsBySeasonAndPlayer(2022,"151756") // left during transfer window
        assertEquals(false,dennis[0].current,"Dennis left Bournemouth during transfer window")
    }
}

/* 
        var allGames = gameRepository.findAll()

        assertEquals(2,allGames.size, "since our pretend premier league only has two teams, there are only two fixtures where they play each other twice, each once at home") 
        // since our pretend premier league only has two teams, there are only two fixtures where they play each other twice, each once at home

                // since a game was rescheduled check that the postponed one was deleted from DB and new one was added
        allGames = gameRepository.findAll()
        assertEquals(allGames.size,2,"postponed game should be deleted from db, new game added")
        val firstGameId = allGames[0].fixtureId
        val secondGameId = allGames[1].fixtureId
        assertTrue(firstGameId=="86824099" || secondGameId == "86824099")
*/



