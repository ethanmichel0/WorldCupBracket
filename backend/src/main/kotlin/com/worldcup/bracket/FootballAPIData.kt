package com.worldcup.bracket

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.beans.factory.annotation.Autowired

import java.util.Date
import java.util.Calendar
import java.text.SimpleDateFormat

@Component
class FootballAPIData(private val secretsConfigurationProperties : SecretsConfigurationProperties) {

    @Value("\${footballAPI.baseAPI}")
    lateinit var BASE_API: String

    fun getAllFixturesInSeasonEndpoint(league: String, season: Int) = BASE_API + "fixtures?season=${season}&league=${league}"

    fun getAllUpcomingFixturesInSeasonEndpoint(league: String, season: Int) : String {

        // NOTE: this doesn't get the actual last day of the season (e.g. May 25th) but rather will return December 31st, 2023 for the 22-23 season 
        // or December 31st 2024 for 23-24 season. This is so we can retrieve all games from the API from the current date to the end of the season
        // more generically (different leagues may have different end dates though if we use December 31st then we can always get the rest of the games
        // for the current season)

        val c = Calendar.getInstance()
        c.set(Calendar.YEAR, season+1)
        c.set(Calendar.MONTH,11)
        c.set(Calendar.DAY_OF_MONTH,31)

        val formattedDateEndOfSeason = SimpleDateFormat("yyyy-MM-dd").format(c.getTime())

        val c2 = Calendar.getInstance()
        c2.setTime(Date())
        val formattedDateToday = SimpleDateFormat("yyyy-MM-dd").format(c2.getTime())
        val finalEndpoint = BASE_API + "fixtures?season=${season}&league=${league}?from=${formattedDateToday}&to=${formattedDateEndOfSeason}"
        return finalEndpoint
    }

    fun getLeagueEndpoint(league: String) = BASE_API + "leagues?id=${league}"

    fun getStandingsEndpoint(league: String, season: Int) = BASE_API + "standings?league=${league}&season=${season}"

    fun getSingleFixtureEndpoint(id : String) = BASE_API + "fixtures?id=${id}"

    fun getAllPlayersOnTeamEndpoint(team : String) = BASE_API + "players/squads?team=${team}"

    fun getTeamInfoEndpoint(team : String) = BASE_API + "teams?id=${team}"

    val X_RAPID_API_HOST = "v3.football.api-sports.io"
    val FOOTBALL_API_KEY: String = secretsConfigurationProperties.footballApiKey
    val STATUS_FINISHED_SHORT = "FT"
    val STATUS_POSTPONED_SHORT = "PST"
    val STATUS_SUSPENDED_SHORT = "SUSP"
    val STATUS_ABANDONED_SHORT = "ABD"
}