package com.worldcup.bracket

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.beans.factory.annotation.Autowired


@Component
class FootballAPIData(private val secretsConfigurationProperties : SecretsConfigurationProperties) {

    @Value("\${footballAPI.baseAPI}")
    lateinit var BASE_API: String

    fun setFixturesAPI(league: String, season: String) = BASE_API + "fixtures?season=${season}&league=${league}"

    fun setLeagueAPI(league: String) = BASE_API + "leagues?id=${league}"

    fun setStandingsAPI(league: String, season: Int) = BASE_API + "standings?league={league}&season={season}"

    fun setSingleFixtureAPI(id : String) = BASE_API + "fixtures?id=${id}"

    fun getAllPlayersOnTeam(team : String) = BASE_API + "players/squads?team=${team}"
    // API uses pagination    


    val X_RAPID_API_HOST = "v3.football.api-sports.io"
    val FOOTBALL_API_KEY: String = secretsConfigurationProperties.footballApiKey
    val STATUS_FINISHED = "Match Finished"
}