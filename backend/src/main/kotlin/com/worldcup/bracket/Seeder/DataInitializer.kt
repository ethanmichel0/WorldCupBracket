package com.worldcup.bracket.Seeder

import org.springframework.boot.ApplicationRunner
import org.springframework.stereotype.Component
import org.springframework.boot.ApplicationArguments
import java.nio.file.Paths
import com.google.gson.Gson; 
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken

import com.worldcup.bracket.repository.GroupRepository
import com.worldcup.bracket.repository.TeamRepository
import com.worldcup.bracket.repository.GameRepository
import com.worldcup.bracket.Entity.Group
import com.worldcup.bracket.Entity.Game
import com.worldcup.bracket.Entity.Team
import com.worldcup.bracket.DTO.FixturesAPIResponseWrapper
import com.worldcup.bracket.Service.BuildNewRequest

import java.io.File
import java.util.Date
import java.util.ArrayList
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.net.URI


@Component()
class DataInitializer(
    private val groupRepository: GroupRepository,
    private val teamRepository: TeamRepository,
    private val gameRepository: GameRepository
    ) : ApplicationRunner {

    override fun run (args: ApplicationArguments) {
        if (this.groupRepository.findAll().size == 0) { // only initialize groups one time
            // get all fixtures from football api
            val request = BuildNewRequest(Constants.FIXTURES_API,"GET",null,"x-rapidapi-host",Constants.X_RAPID_API_HOST,"x-rapidapi-key",Constants.FOOTBALL_API_KEY)

            val response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
            val responseWrapper : FixturesAPIResponseWrapper = Gson().fromJson(response.body(), FixturesAPIResponseWrapper::class.java)
            
            val path: String = Paths.get("").toAbsolutePath().toString()
            val jsonString: String = File(path + "/src/groups.json").readText(Charsets.UTF_8)
            val groupsWrapper : GroupsWrapper = Gson().fromJson(jsonString, GroupsWrapper::class.java)
            val allGames = mutableListOf<Game>()
            for (group in groupsWrapper.groups) {
                for (i in 0 until group.teams.size) {
                    for (j in 1 until group.teams.size) 
                        if(group.teams[i] != group.teams[j]) {
                            // find matching game from response from api (responseWrapper) so that we can get time of game, and logo
                            // for each team
                            val matchingFixture = responseWrapper.response.filter{(it.teams.home == group.teams[i] && 
                                    it.teams.away == group.teams[j])
                                    || (it.teams.away == group.teams[i] 
                                    && it.teams.home == group.teams[j])}[0]
                            if (matchingFixture.teams.home == group.teams[i]) {
                                group.teams[i].logo = matchingFixture.teams.home.logo
                                group.teams[j].logo = matchingFixture.teams.away.logo
                                group.teams[i].id = matchingFixture.teams.home.id
                                group.teams[j].id = matchingFixture.teams.away.id
                            } else {
                                group.teams[j].logo = matchingFixture.teams.home.logo
                                group.teams[i].logo = matchingFixture.teams.away.logo
                                group.teams[j].id = matchingFixture.teams.home.id
                                group.teams[i].id = matchingFixture.teams.away.id
                            }
                            allGames.add(Game(group.teams[i],group.teams[j],false,matchingFixture.fixture.timestamp.toLong(),matchingFixture.fixture.id))
                        }
                }
            }
            var allTeams = mutableListOf<Team>()
            for (group in groupsWrapper.groups) {
                allTeams.addAll(group.teams)
            }

            groupRepository.saveAll(groupsWrapper.groups)
            teamRepository.saveAll(allTeams)
            gameRepository.saveAll(allGames)
        }
    }
}

data class GroupsWrapper(
    val groups: List<Group>
)