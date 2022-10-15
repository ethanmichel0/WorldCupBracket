package com.worldcup.bracket.Service

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.data.mongo.AutoConfigureDataMongo
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ContextConfiguration

import com.worldcup.bracket.Entity.Team
import com.worldcup.bracket.Entity.Game
import com.worldcup.bracket.FootballAPIData
import com.worldcup.bracket.Repository.GameRepository
import com.worldcup.bracket.Repository.TeamRepository
import com.worldcup.bracket.DTO.OverrideGroupSettings
import com.worldcup.bracket.SecretsConfigurationProperties


import org.springframework.data.repository.findByIdOrNull



@AutoConfigureDataMongo
@SpringBootTest
class GroupStandingsTests {

    @Autowired 
    private lateinit var footballAPIData: FootballAPIData

    @Autowired 
    private lateinit var teamService: TeamService

    @Autowired
    private lateinit var gameRepository: GameRepository

    @Autowired 
    private lateinit var teamRepository: TeamRepository

    @Autowired
    private lateinit var secretsConfigurationProperties : SecretsConfigurationProperties


    @Test
    fun `standings`() {
        val apiResponseFileName = "initialResults.json"
        val apiResponse: String? = this::class.java.classLoader.getResource(apiResponseFileName)?.readText()

        var qatar : Team = teamRepository.findByName("Qatar")[0]
        var ecuador : Team = teamRepository.findByName("Ecuador")[0]
        var senegal : Team = teamRepository.findByName("Senegal")[0]
        var netherlands : Team = teamRepository.findByName("Netherlands")[0]

        qatar.winsGroup = 3
        ecuador.winsGroup = 2
        ecuador.lossesGroup = 1
        senegal.winsGroup = 1
        senegal.lossesGroup = 2
        netherlands.lossesGroup = 3

        teamRepository.saveAll(listOf(qatar,ecuador,senegal,netherlands))
        teamService.sortTeamsInGroup("A")

        qatar = teamRepository.findByName("Qatar")[0]
        ecuador = teamRepository.findByName("Ecuador")[0]

        assert(qatar.positionGroup==1)
        assert(ecuador.positionGroup==2)

        var england : Team = teamRepository.findByName("England")[0]
        var iran : Team = teamRepository.findByName("Iran")[0]
        var usa : Team = teamRepository.findByName("USA")[0]
        var wales : Team = teamRepository.findByName("Wales")[0]

        england.winsGroup = 2
        england.ties = 1
        england.goalsForGroup = 10
        england.goalsAgainstGroup = 6
        iran.winsGroup = 2
        iran.ties = 1
        iran.goalsForGroup = 11
        iran.goalsAgainstGroup = 9
        usa.winsGroup = 1
        usa.lossesGroup = 2
        wales.lossesGroup = 3

        teamRepository.saveAll(listOf(england,iran,usa,wales))
        teamService.sortTeamsInGroup("B")

        england = teamRepository.findByName("England")[0]
        iran = teamRepository.findByName("Iran")[0]

        assert(england.positionGroup==1)
        assert(iran.positionGroup==2)

        var saudiArabia : Team = teamRepository.findByName("Saudi Arabia")[0]
        var mexico : Team = teamRepository.findByName("Mexico")[0]
        var argentina : Team = teamRepository.findByName("Argentina")[0]
        var poland : Team = teamRepository.findByName("Poland")[0]

        poland.winsGroup = 3
        mexico.winsGroup = 1
        mexico.ties = 1
        mexico.lossesGroup = 1
        mexico.goalsForGroup = 10
        mexico.goalsAgainstGroup = 6
        argentina.winsGroup = 1
        argentina.ties = 1
        argentina.lossesGroup = 1
        argentina.goalsForGroup = 11
        argentina.goalsAgainstGroup = 7
        saudiArabia.lossesGroup = 3

        teamRepository.saveAll(listOf(saudiArabia,mexico,argentina,poland))
        teamService.sortTeamsInGroup("C")

        poland = teamRepository.findByName("Poland")[0]
        argentina = teamRepository.findByName("Argentina")[0]
        mexico = teamRepository.findByName("Mexico")[0]

        assert(poland.positionGroup==1)
        assert(argentina.positionGroup==2)
        assert(mexico.positionGroup==3)

        var france = teamRepository.findByName("France")[0]
        var australia = teamRepository.findByName("Australia")[0]
        var denmark = teamRepository.findByName("Denmark")[0]
        var tunisia = teamRepository.findByName("Tunisia")[0]

        france.ties = 3
        australia.ties = 3
        denmark.ties = 3
        tunisia.ties = 3

        teamRepository.saveAll(listOf(france,australia,denmark,tunisia))
        teamService.sortTeamsInGroup("D")

        teamService.overrideGroupOrdering(OverrideGroupSettings("Tunisia", "Denmark","Australia", "France", "D", secretsConfigurationProperties.overridePw))
        // need to manually specify tiebreaker in this case

        france = teamRepository.findByName("France")[0]
        australia = teamRepository.findByName("Australia")[0]
        denmark = teamRepository.findByName("Denmark")[0]
        tunisia = teamRepository.findByName("Tunisia")[0]
        
        assert(tunisia.positionGroup == 1)
        assert(denmark.positionGroup == 2)
        assert(australia.positionGroup == 3)
        assert(france.positionGroup == 4)
    }
}