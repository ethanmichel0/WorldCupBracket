package com.worldcup.bracket

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.beans.factory.annotation.Autowired


@Component
class FootballAPIData {
    @Value("\${footballAPI.baseAPI}")
    lateinit var BASE_API: String
    @Value("\${footballAPI.baseAPI}fixtures?season=2022&league=1")
    lateinit var FIXTURES_API: String
    fun setSingleFixtureAPI(id : String) = BASE_API + "fixtures?id=${id}"
    // These must be "var" because initialized according to application.properties file but in reality these are constants


    val X_RAPID_API_HOST = "v3.football.api-sports.io"
    @Value("\${secrets.FOOTBALL_API_KEY}")
    lateinit var FOOTBALL_API_KEY: String
    val STATUS_FINISHED = "Match Finished"
}