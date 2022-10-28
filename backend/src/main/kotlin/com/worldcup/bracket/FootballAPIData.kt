package com.worldcup.bracket

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.beans.factory.annotation.Autowired


@Component
class FootballAPIData(private val secretsConfigurationProperties : SecretsConfigurationProperties) {

    @Value("\${footballAPI.baseAPI}")
    lateinit var BASE_API: String
    @Value("\${footballAPI.baseAPI}fixtures?season=2022&league=1")
    lateinit var FIXTURES_API: String
    fun setSingleFixtureAPI(id : String) = BASE_API + "fixtures?id=${id}"

    fun getAllPlayersOnTeam(team : String) = BASE_API + "players?team=${team}&league=1&season=2018&page=1"
    // API uses pagination    


    val X_RAPID_API_HOST = "v3.football.api-sports.io"
    val FOOTBALL_API_KEY: String = secretsConfigurationProperties.footballApiKey
    val STATUS_FINISHED = "Match Finished"
}