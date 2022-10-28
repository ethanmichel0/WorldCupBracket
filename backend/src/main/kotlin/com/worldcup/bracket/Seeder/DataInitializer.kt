package com.worldcup.bracket.Seeder

import org.springframework.boot.ApplicationRunner
import org.springframework.stereotype.Component
import org.springframework.boot.ApplicationArguments
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Profile;
import java.nio.file.Paths
import com.google.gson.Gson; 
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken

import com.worldcup.bracket.Repository.TeamRepository
import com.worldcup.bracket.Repository.GameRepository
import com.worldcup.bracket.Repository.PlayerRepository
import com.worldcup.bracket.Entity.Game
import com.worldcup.bracket.Entity.Team
import com.worldcup.bracket.Entity.Player
import com.worldcup.bracket.DTO.FixturesAPIResponseWrapper
import com.worldcup.bracket.DTO.PlayersAPIResponseWrapper
import com.worldcup.bracket.Service.BuildNewRequest
import com.worldcup.bracket.FootballAPIData

import java.io.File
import java.util.Date
import java.util.ArrayList
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.net.URI
import kotlinx.coroutines.*

@Profile("!skipDataInitialization")
@Component
class DataInitializer(
    private val teamRepository: TeamRepository,
    private val gameRepository: GameRepository,
    private val playerRepository: PlayerRepository,
    private var footballAPIData: FootballAPIData
    ) : ApplicationRunner {

    val httpClient = HttpClient.newHttpClient()

    override fun run (args: ApplicationArguments) {
        
        if (this.teamRepository.findAll().size != 0) {
            this.teamRepository.deleteAll()
            this.gameRepository.deleteAll()
            this.playerRepository.deleteAll()
        }

        // get all fixtures from football api
        val request = BuildNewRequest(footballAPIData.FIXTURES_API,"GET",null,"x-rapidapi-host",footballAPIData.X_RAPID_API_HOST,"x-rapidapi-key",footballAPIData.FOOTBALL_API_KEY)
        val response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        val responseWrapper : FixturesAPIResponseWrapper = Gson().fromJson(response.body(), FixturesAPIResponseWrapper::class.java)
        
        val path: String = Paths.get("").toAbsolutePath().toString()
        val jsonString: String = File(path + "/src/groups.json").readText(Charsets.UTF_8)
        val teamsWrapper : TeamsWrapper = Gson().fromJson(jsonString, TeamsWrapper::class.java)
        val allGames = mutableListOf<Game>()
        val allPlayers = mutableListOf<Player>()
        for (i in 0 until teamsWrapper.teams.size) {
            for (j in 1 until teamsWrapper.teams.size)  {
                if(i != j && teamsWrapper.teams[i].group == teamsWrapper.teams[j].group) {
                    // find matching game from response from api (responseWrapper) so that we can get time of game, and logo
                    // for each team
                    val matchingFixture = responseWrapper.response.filter{(it.teams.home == teamsWrapper.teams[i] && 
                            it.teams.away == teamsWrapper.teams[j])
                            || (it.teams.away == teamsWrapper.teams[i] 
                            && it.teams.home == teamsWrapper.teams[j])}[0]
                    if (matchingFixture.teams.home == teamsWrapper.teams[i]) {
                        teamsWrapper.teams[i].logo = matchingFixture.teams.home.logo
                        teamsWrapper.teams[j].logo = matchingFixture.teams.away.logo
                        teamsWrapper.teams[i].id = matchingFixture.teams.home.id
                        teamsWrapper.teams[j].id = matchingFixture.teams.away.id
                    } else {
                        teamsWrapper.teams[j].logo = matchingFixture.teams.home.logo
                        teamsWrapper.teams[i].logo = matchingFixture.teams.away.logo
                        teamsWrapper.teams[j].id = matchingFixture.teams.home.id
                        teamsWrapper.teams[i].id = matchingFixture.teams.away.id
                    }
                    allGames.add(Game(matchingFixture.teams.home,
                            matchingFixture.teams.away,
                            teamsWrapper.teams[i].group,
                            false,
                            matchingFixture.fixture.timestamp.toLong(),
                            matchingFixture.fixture.id))
                }
            }
        }

        runBlocking {
            val allPlayersAdded = teamsWrapper.teams.map {
                async(Dispatchers.IO) { 
                    addPlayersFromTeam(it)
                }
            }.awaitAll()
            for (playersOnOneTeam in allPlayersAdded) {
                allPlayers.addAll(playersOnOneTeam)
            }
        }

        teamRepository.saveAll(teamsWrapper.teams)
        gameRepository.saveAll(allGames)
        playerRepository.saveAll(allPlayers)
    }

    suspend fun addPlayersFromTeam(team: Team) : List<Player> {
        val playersRequest = BuildNewRequest(footballAPIData.getAllPlayersOnTeam(team.id!!),"GET",null,"x-rapidapi-host",footballAPIData.X_RAPID_API_HOST,"x-rapidapi-key",footballAPIData.FOOTBALL_API_KEY)
        val playersResponse = httpClient.send(playersRequest, HttpResponse.BodyHandlers.ofString());
        val playersResponseWrapper : PlayersAPIResponseWrapper = Gson().fromJson(playersResponse.body(), PlayersAPIResponseWrapper::class.java)
        val playersList = mutableListOf<Player>()
        for (player in playersResponseWrapper.response) {
            playersList.add(Player(
                team,
                player.statistics[0].games.position,
                player.player.id,
                player.player.firstname,
                player.player.lastname,
                player.player.age,
                player.player.height,
                ))
        }
        return playersList 
    }
}

data class TeamsWrapper(
    val teams: List<Team>
)