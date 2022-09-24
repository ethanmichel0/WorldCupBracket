package com.worldcup.bracket.Service 

import org.springframework.stereotype.Service
import org.springframework.data.repository.findByIdOrNull

import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import com.google.gson.Gson; 
import com.google.gson.GsonBuilder;

import com.worldcup.bracket.DTO.FixturesAPIResponseWrapper
import com.worldcup.bracket.repository.GameRepository
import com.worldcup.bracket.repository.TeamRepository
import com.worldcup.bracket.Entity.Game
import com.worldcup.bracket.Entity.Team

import java.util.Comparator

@Service
class GameService(private val gameRepository : GameRepository, private val teamRepository : TeamRepository) {

    private fun updateScores(fixtureId : String) {
        val request = BuildNewRequest("${Constants.FIXTURES_API}/id=${fixtureId}","GET",null,"x-rapidapi-host",Constants.X_RAPID_API_HOST,"x-rapidapi-key",Constants.FOOTBALL_API_KEY)
        val response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
        val responseWrapper : FixturesAPIResponseWrapper = Gson().fromJson(response.body(), FixturesAPIResponseWrapper::class.java)
        val game : Game? = gameRepository.findByIdOrNull(responseWrapper.response[0].fixture.id)
        if (game != null) {
            if (responseWrapper.response[0].fixture.status.long == Constants.STATUS_FINISHED) {
                if (responseWrapper.response[0].goals.home != null && responseWrapper.response[0].goals.away != null) {
                    if (responseWrapper.response[0].goals.home!! > responseWrapper.response[0].goals.away!! || 
                            ((responseWrapper.response[0].score.penalty.home != null && responseWrapper.response[0].score.penalty.away != null) &&
                            responseWrapper.response[0].score.penalty.home!! > responseWrapper.response[0].score.penalty.away!!)) {
                        game.winner = responseWrapper.response[0].teams.home
                        if (game.knockoutGame) {
                            game.home.winsKnockout ++;
                            game.away.lossesKnockout ++;
                        } else {
                            game.home.winsGroup ++;
                            game.away.lossesGroup ++;
                        }
                    } else if (responseWrapper.response[0].goals.home!! < responseWrapper.response[0].goals.away!! || 
                            ((responseWrapper.response[0].score.penalty.home != null && responseWrapper.response[0].score.penalty.away != null) &&
                            responseWrapper.response[0].score.penalty.away!! > responseWrapper.response[0].score.penalty.home!!)) {
                        game.winner = responseWrapper.response[0].teams.away
                        if (game.knockoutGame) {
                            game.home.lossesKnockout ++;
                            game.away.winsKnockout ++;
                        } else {
                            game.home.lossesGroup ++;
                            game.away.winsGroup ++;
                        }
                    } else {
                        game.home.ties ++;
                        game.away.ties ++;
                        // only ties in group stage, not knockout games.
                    }
                }
            }
        }
    }
}