package com.worldcup.bracket.Service 

import org.springframework.stereotype.Service
import org.springframework.data.repository.findByIdOrNull

import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import com.google.gson.Gson; 
import com.google.gson.GsonBuilder;

import com.worldcup.bracket.DTO.FixturesAPIResponseWrapper
import com.worldcup.bracket.Repository.GameRepository
import com.worldcup.bracket.Repository.TeamRepository
import com.worldcup.bracket.Entity.Game
import com.worldcup.bracket.Entity.Team
import com.worldcup.bracket.Service.BuildNewRequest
import com.worldcup.bracket.FootballAPIData


import java.util.Comparator

@Service
class GameService(private val gameRepository : GameRepository,
    private val teamRepository : TeamRepository,
    private val footballAPIData : FootballAPIData) {

    public fun updateScores(fixtureId : String) {
        println("INSIDERSDF")
        val request = BuildNewRequest(footballAPIData.setSingleFixtureAPI(fixtureId),"GET",null,"x-rapidapi-host",footballAPIData.X_RAPID_API_HOST,"x-rapidapi-key",footballAPIData.FOOTBALL_API_KEY)
        println("REQUEST")
        println(request)
        val response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
        println(response.body())
        println("IS RESPOONSE BOODY")
        val responseWrapper : FixturesAPIResponseWrapper = Gson().fromJson(response.body(), FixturesAPIResponseWrapper::class.java)
        val game : Game? = gameRepository.findByIdOrNull(responseWrapper.response[0].fixture.id)
        println(game)
        println(responseWrapper.response[0])
        println("IS GAME")
        if (game != null) {
                if (responseWrapper.response[0].goals.home != null && responseWrapper.response[0].goals.away != null) {
                    game.homeScore = responseWrapper.response[0].goals.home!!
                    game.awayScore = responseWrapper.response[0].goals.away!!
                    game.currentMinute = responseWrapper.response[0].fixture.status.elapsed!!
                    }
                if (responseWrapper.response[0].fixture.status.long == footballAPIData.STATUS_FINISHED && ! game.scoresAlreadySet) {
                    if (game.knockoutGame) {
                        game.home.goalsForKnockout += responseWrapper.response[0].goals.home!!
                        game.away.goalsForKnockout += responseWrapper.response[0].goals.away!!
                        game.home.goalsAgainstKnockout += responseWrapper.response[0].goals.away!!
                        game.away.goalsAgainstKnockout += responseWrapper.response[0].goals.home!!

                        if (responseWrapper.response[0].goals.home!! > responseWrapper.response[0].goals.away!! ||
                                responseWrapper.response[0].score.penalty.home!! > responseWrapper.response[0].score.penalty.away!!) {
                                    game.winner = game.home
                                    game.home.winsKnockout ++
                                    game.away.lossesKnockout ++
                                } else {
                                    game.winner = game.away
                                    game.away.winsKnockout ++
                                    game.home.lossesKnockout ++
                                }
                    } else {
                        game.home.goalsForGroup += responseWrapper.response[0].goals.home!!
                        game.away.goalsForGroup += responseWrapper.response[0].goals.away!!
                        game.home.goalsAgainstGroup += responseWrapper.response[0].goals.away!!
                        game.away.goalsAgainstGroup += responseWrapper.response[0].goals.home!!
                        if (responseWrapper.response[0].goals.home!! > responseWrapper.response[0].goals.away!!) {
                            game.winner = game.home
                            game.home.winsGroup ++
                            game.away.lossesGroup ++
                        } else if (responseWrapper.response[0].goals.home!! < responseWrapper.response[0].goals.away!!) {
                            game.winner = game.away
                            game.away.winsGroup ++
                            game.home.lossesGroup ++;
                        } else {
                            game.home.ties ++
                            game.away.ties ++
                        }
                    }
                    game.scoresAlreadySet = true;
                }
                gameRepository.save(game)
                teamRepository.saveAll(listOf(game.home,game.away))
        }
    }

    private fun createKnockoutRound(round : String) {
        when (round) {
            "8th Finals" -> {
                val request = BuildNewRequest("${footballAPIData.FIXTURES_API}&round = 8th Finals","GET",null,"x-rapidapi-host",footballAPIData.X_RAPID_API_HOST,"x-rapidapi-key",footballAPIData.FOOTBALL_API_KEY)
                val firstPlaceTeams = teamRepository.findByPositionGroupOrderByGroupAsc(1);
                val secondPlaceTeams = teamRepository.findByPositionGroupOrderByGroupAsc(2);
                val gamesToCreate = mutableListOf<Game>()
                val response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
                val responseWrapper : FixturesAPIResponseWrapper = Gson().fromJson(response.body(), FixturesAPIResponseWrapper::class.java)
                
                findAndAddRelevantKnockoutGame(firstPlaceTeams[0],secondPlaceTeams[1],responseWrapper,gamesToCreate,"8th Finals",0)
                findAndAddRelevantKnockoutGame(firstPlaceTeams[1],secondPlaceTeams[0],responseWrapper,gamesToCreate,"8th Finals",1)
                findAndAddRelevantKnockoutGame(firstPlaceTeams[2],secondPlaceTeams[3],responseWrapper,gamesToCreate,"8th Finals",2)
                findAndAddRelevantKnockoutGame(firstPlaceTeams[3],secondPlaceTeams[2],responseWrapper,gamesToCreate,"8th Finals",3)
                findAndAddRelevantKnockoutGame(firstPlaceTeams[4],secondPlaceTeams[5],responseWrapper,gamesToCreate,"8th Finals",4)
                findAndAddRelevantKnockoutGame(firstPlaceTeams[5],secondPlaceTeams[4],responseWrapper,gamesToCreate,"8th Finals",5)
                findAndAddRelevantKnockoutGame(firstPlaceTeams[6],secondPlaceTeams[7],responseWrapper,gamesToCreate,"8th Finals",6)
                findAndAddRelevantKnockoutGame(firstPlaceTeams[7],secondPlaceTeams[6],responseWrapper,gamesToCreate,"8th Finals",7)

                gameRepository.saveAll(gamesToCreate)
            }
            "Quarter-Finals" -> {
                val request = BuildNewRequest("${footballAPIData.FIXTURES_API}&round = Quarter-Finals","GET",null,"x-rapidapi-host",footballAPIData.X_RAPID_API_HOST,"x-rapidapi-key",footballAPIData.FOOTBALL_API_KEY)
                val gamesToCreate = mutableListOf<Game>()
                val response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
                val responseWrapper : FixturesAPIResponseWrapper = Gson().fromJson(response.body(), FixturesAPIResponseWrapper::class.java)
                val winnersFromRoundOf16 = mutableListOf<Team>()
                val roundOf16Games = gameRepository.findByGroupOrderByGameNumberAsc("8th Finals");
                for (game in roundOf16Games) {
                    winnersFromRoundOf16.add(game.winner!!)
                }

                findAndAddRelevantKnockoutGame(winnersFromRoundOf16[0],winnersFromRoundOf16[1],responseWrapper,gamesToCreate,"Quarter-Finals",0)
                findAndAddRelevantKnockoutGame(winnersFromRoundOf16[2],winnersFromRoundOf16[3],responseWrapper,gamesToCreate,"Quarter-Finals",1)
                findAndAddRelevantKnockoutGame(winnersFromRoundOf16[4],winnersFromRoundOf16[5],responseWrapper,gamesToCreate,"Quarter-Finals",2)
                findAndAddRelevantKnockoutGame(winnersFromRoundOf16[6],winnersFromRoundOf16[7],responseWrapper,gamesToCreate,"Quarter-Finals",3)

                gameRepository.saveAll(gamesToCreate)
            }
            "Semi-Finals" -> {
                val request = BuildNewRequest("${footballAPIData.FIXTURES_API}&round = Semi-Finals","GET",null,"x-rapidapi-host",footballAPIData.X_RAPID_API_HOST,"x-rapidapi-key",footballAPIData.FOOTBALL_API_KEY)
                val gamesToCreate = mutableListOf<Game>()
                val response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
                val responseWrapper : FixturesAPIResponseWrapper = Gson().fromJson(response.body(), FixturesAPIResponseWrapper::class.java)
                val winnersFromQuarterFinals = mutableListOf<Team>()
                val quarterFinals = gameRepository.findByGroupOrderByGameNumberAsc("Quarter-Finals");
                for (game in quarterFinals) {
                    winnersFromQuarterFinals.add(game.winner!!)
                }

                findAndAddRelevantKnockoutGame(winnersFromQuarterFinals[0],winnersFromQuarterFinals[1],responseWrapper,gamesToCreate,"Semi-Finals",0)
                findAndAddRelevantKnockoutGame(winnersFromQuarterFinals[2],winnersFromQuarterFinals[3],responseWrapper,gamesToCreate,"Semi-Finals",1)
                
                gameRepository.saveAll(gamesToCreate)
            }
            "Final" -> {
                val request = BuildNewRequest("${footballAPIData.FIXTURES_API}&round = Final","GET",null,"x-rapidapi-host",footballAPIData.X_RAPID_API_HOST,"x-rapidapi-key",footballAPIData.FOOTBALL_API_KEY)
                val response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
                val responseWrapper : FixturesAPIResponseWrapper = Gson().fromJson(response.body(), FixturesAPIResponseWrapper::class.java)
                val winnersFromSemiFinals = mutableListOf<Team>()
                val semiFinals = gameRepository.findByGroupOrderByGameNumberAsc("Semi-Finals");
                for (game in semiFinals) {
                    winnersFromSemiFinals.add(game.winner!!)
                }

                gameRepository.save(
                    Game(winnersFromSemiFinals[0],
                        winnersFromSemiFinals[1],
                        "Final",
                        true,
                        responseWrapper.response[0].fixture.timestamp.toLong(),
                        responseWrapper.response[0].fixture.id,
                        1))
            }
        }
    }

    private fun findAndAddRelevantKnockoutGame(home : Team, away : Team, wrapper : FixturesAPIResponseWrapper, games : MutableList<Game>, round : String, gameNum : Int) {
        val game = wrapper.response.filter{(it.teams.home == home && 
                            it.teams.away == away)}[0]
                games.add(
                    Game(home,
                        away,
                        round,
                        true,
                        game.fixture.timestamp.toLong(),
                        game.fixture.id,
                        gameNum))
    }
}