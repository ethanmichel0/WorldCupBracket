package com.worldcup.bracket

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.beans.factory.annotation.Autowired


@Component
class FootballAPIData {
    @Value("\${footballAPI.baseAPI}")
    lateinit var BASE_API: String
    val FIXTURES_API = "https://v3.football.api-sports.io/fixtures?season=2022&league=1"
    fun setSingleFixtureAPI(id : String) = BASE_API + "fixtures?id=${id}"
    // These must be "var" because initialized according to application.properties file but in reality these are constants


    val X_RAPID_API_HOST = "v3.football.api-sports.io"
    val FOOTBALL_API_KEY = System.getenv("FOOTBALL_API_KEY")
    val STATUS_FINISHED = "Match Finished"
}