package com.worldcup.bracket

import com.worldcup.bracket.DTO.PlayersAPIResponseWrapper

import com.worldcup.bracket.Entity.Team
import com.worldcup.bracket.Entity.Player
import com.worldcup.bracket.Service.BuildNewRequest
import com.worldcup.bracket.FootballAPIData

import com.google.gson.Gson; 
import com.google.gson.GsonBuilder;
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

fun addPlayersFromTeam(team: Team, footballAPIData: FootballAPIData) : List<Player> {
    println("IN ADD PLAYERS FROM TEAM AND URL IS ${footballAPIData.getAllPlayersOnTeam(team.id!!)}")
    val httpClient = HttpClient.newHttpClient()
    val playersRequest = BuildNewRequest(footballAPIData.getAllPlayersOnTeam(team.id!!),"GET",null,"x-rapidapi-host",footballAPIData.X_RAPID_API_HOST,"x-rapidapi-key",footballAPIData.FOOTBALL_API_KEY)
    val playersResponse = httpClient.send(playersRequest, HttpResponse.BodyHandlers.ofString());
    val playersResponseWrapper : PlayersAPIResponseWrapper = Gson().fromJson(playersResponse.body(), PlayersAPIResponseWrapper::class.java)
    val playersList = mutableListOf<Player>()
    for (player in playersResponseWrapper.response[0].players) {
        playersList.add(Player(
            team = team,
            id = player.id,
            name = player.name,
            age = player.age,
            number = player.number,
            position = player.position
            ))
    }
    return playersList 
}