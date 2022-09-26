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
import com.worldcup.bracket.Service.BuildNewRequest


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
                    if (responseWrapper.response[0].goals.home!! > responseWrapper.response[0].goals.away!!) {
                        game.winner = responseWrapper.response[0].teams.home
                        if (game.knockoutGame) {
                            game.home.winsKnockout ++;
                            game.away.lossesKnockout ++;
                            game.home.goalsForKnockout += responseWrapper.response[0].goals.home!!
                            game.away.goalsForKnockout += responseWrapper.response[0].goals.away!!
                            game.home.goalsAgainstKnockout += responseWrapper.response[0].goals.away!!
                            game.away.goalsAgainstKnockout += responseWrapper.response[0].goals.home!!
                        } else {
                            game.home.winsGroup ++;
                            game.away.lossesGroup ++;
                            game.home.goalsForGroup += responseWrapper.response[0].goals.home!!
                            game.away.goalsForGroup += responseWrapper.response[0].goals.away!!
                            game.home.goalsAgainstGroup += responseWrapper.response[0].goals.away!!
                            game.away.goalsAgainstGroup += responseWrapper.response[0].goals.home!!
                        }
                    } else if (responseWrapper.response[0].goals.home!! < responseWrapper.response[0].goals.away!!) {
                        game.winner = responseWrapper.response[0].teams.away
                        if (game.knockoutGame) {
                            game.home.lossesKnockout ++;
                            game.away.winsKnockout ++;
                            game.home.goalsForKnockout += responseWrapper.response[0].goals.home!!
                            game.away.goalsForKnockout += responseWrapper.response[0].goals.away!!
                            game.home.goalsAgainstKnockout += responseWrapper.response[0].goals.away!!
                            game.away.goalsAgainstKnockout += responseWrapper.response[0].goals.home!!
                        } else {
                            game.home.lossesGroup ++;
                            game.away.winsGroup ++;
                            game.home.goalsForGroup += responseWrapper.response[0].goals.home!!
                            game.home.goalsForGroup += responseWrapper.response[0].goals.away!!
                            game.home.goalsAgainstGroup += responseWrapper.response[0].goals.away!!
                            game.away.goalsAgainstGroup += responseWrapper.response[0].goals.home!!
                        }
                    }
                    // TODO add testing for all of these scenarios
                    // only penalties in knockout games (no ties)
                    else if (responseWrapper.response[0].score.penalty.home != null && responseWrapper.response[0].score.penalty.away != null &&
                            responseWrapper.response[0].score.penalty.home!! > responseWrapper.response[0].score.penalty.away!!) {
                        game.home.winsKnockout ++;
                        game.away.lossesKnockout ++;
                        game.home.goalsForKnockout += responseWrapper.response[0].goals.home!!
                            game.away.goalsForKnockout += responseWrapper.response[0].goals.away!!
                            game.home.goalsAgainstKnockout += responseWrapper.response[0].goals.away!!
                            game.away.goalsAgainstKnockout += responseWrapper.response[0].goals.home!!
                    }
                    else if (responseWrapper.response[0].score.penalty.home != null && responseWrapper.response[0].score.penalty.away != null &&
                            responseWrapper.response[0].score.penalty.home!! < responseWrapper.response[0].score.penalty.away!!) {
                        game.away.winsKnockout ++;
                        game.home.lossesKnockout ++;
                        game.home.goalsForKnockout += responseWrapper.response[0].goals.home!!
                            game.away.goalsForKnockout += responseWrapper.response[0].goals.away!!
                            game.home.goalsAgainstKnockout += responseWrapper.response[0].goals.away!!
                            game.away.goalsAgainstKnockout += responseWrapper.response[0].goals.home!!
                    } else {
                        game.home.ties ++;
                        game.away.ties ++;
                        // only ties in group stage, not knockout games.
                        game.home.goalsForGroup += responseWrapper.response[0].goals.home!!
                        game.home.goalsForGroup += responseWrapper.response[0].goals.away!!
                        game.home.goalsAgainstGroup += responseWrapper.response[0].goals.away!!
                        game.away.goalsAgainstGroup += responseWrapper.response[0].goals.home!!
                    }
                    gameRepository.save(game)
                    teamRepository.saveAll(listOf(game.home,game.away))
                }
            }
        }
    }

    private fun createKnockoutRound(round : String) {
        when (round) {
            "8th Finals" -> {
                val request = BuildNewRequest("${Constants.FIXTURES_API}&round = 8th Finals","GET",null,"x-rapidapi-host",Constants.X_RAPID_API_HOST,"x-rapidapi-key",Constants.FOOTBALL_API_KEY)
                val firstPlaceTeams = teamRepository.findByPositionGroupOrderByGroupAsc(1);
                val secondPlaceTeams = teamRepository.findByPositionGroupOrderByGroupAsc(2);
                val gamesToCreate = mutableListOf<Game>()
                val response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
                val responseWrapper : FixturesAPIResponseWrapper = Gson().fromJson(response.body(), FixturesAPIResponseWrapper::class.java)
                
                val firstGame = responseWrapper.response.filter{(it.teams.home == firstPlaceTeams[0] && 
                            it.teams.away == secondPlaceTeams[1])}[0]
                gamesToCreate.add(
                    Game(firstPlaceTeams[0],
                        secondPlaceTeams[1],
                        "8th Finals",
                        true,
                        firstGame.fixture.timestamp.toLong(),
                        firstGame.fixture.id,
                        1))
                
                val secondGame = responseWrapper.response.filter{(it.teams.home == firstPlaceTeams[1] && 
                            it.teams.away == secondPlaceTeams[0])}[0]
                gamesToCreate.add(
                    Game(firstPlaceTeams[1],
                        secondPlaceTeams[0],
                        "8th Finals",
                        true,
                        secondGame.fixture.timestamp.toLong(),
                        secondGame.fixture.id,
                        2))
                
                val thirdGame = responseWrapper.response.filter{(it.teams.home == firstPlaceTeams[2] && 
                            it.teams.away == secondPlaceTeams[3])}[0]
                gamesToCreate.add(
                    Game(firstPlaceTeams[2],
                        secondPlaceTeams[3],
                        "8th Finals",
                        true,
                        thirdGame.fixture.timestamp.toLong(),
                        thirdGame.fixture.id,
                        3))
                
                val fourthGame = responseWrapper.response.filter{(it.teams.home == firstPlaceTeams[3] && 
                            it.teams.away == secondPlaceTeams[2])}[0]
                gamesToCreate.add(
                    Game(firstPlaceTeams[3],
                        secondPlaceTeams[2],
                        "8th Finals",
                        true,
                        fourthGame.fixture.timestamp.toLong(),
                        fourthGame.fixture.id,
                        4))

                val fifthGame = responseWrapper.response.filter{(it.teams.home == firstPlaceTeams[4] && 
                            it.teams.away == secondPlaceTeams[5])}[0]
                gamesToCreate.add(
                    Game(firstPlaceTeams[4],
                        secondPlaceTeams[5],
                        "8th Finals",
                        true,
                        fifthGame.fixture.timestamp.toLong(),
                        fifthGame.fixture.id,
                        5))
                
                val sixthGame = responseWrapper.response.filter{(it.teams.home == firstPlaceTeams[5] && 
                            it.teams.away == secondPlaceTeams[4])}[0]
                gamesToCreate.add(
                    Game(firstPlaceTeams[5],
                        secondPlaceTeams[4],
                        "8th Finals",
                        true,
                        sixthGame.fixture.timestamp.toLong(),
                        sixthGame.fixture.id,
                        6))
                
                val seventhGame = responseWrapper.response.filter{(it.teams.home == firstPlaceTeams[6] && 
                            it.teams.away == secondPlaceTeams[7])}[0]
                gamesToCreate.add(
                    Game(firstPlaceTeams[6],
                        secondPlaceTeams[7],
                        "8th Finals",
                        true,
                        seventhGame.fixture.timestamp.toLong(),
                        seventhGame.fixture.id,
                        7))
                
                val eigthGame = responseWrapper.response.filter{(it.teams.home == firstPlaceTeams[7] && 
                            it.teams.away == secondPlaceTeams[6])}[0]
                gamesToCreate.add(
                    Game(firstPlaceTeams[7],
                        secondPlaceTeams[6],
                        "8th-Finals",
                        true,
                        eigthGame.fixture.timestamp.toLong(),
                        eigthGame.fixture.id,
                        8))
                gameRepository.saveAll(gamesToCreate)
            }
            "Quarter-Finals" -> {
                val request = BuildNewRequest("${Constants.FIXTURES_API}&round = Quarter-Finals","GET",null,"x-rapidapi-host",Constants.X_RAPID_API_HOST,"x-rapidapi-key",Constants.FOOTBALL_API_KEY)
                val gamesToCreate = mutableListOf<Game>()
                val response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
                val responseWrapper : FixturesAPIResponseWrapper = Gson().fromJson(response.body(), FixturesAPIResponseWrapper::class.java)
                val winnersFromRoundOf16 = mutableListOf<Team>()
                val roundOf16Games = gameRepository.findByGroupOrderByGroupAsc("8th Finals");
                for (game in roundOf16Games) {
                    winnersFromRoundOf16.add(game.winner!!)
                }

                val firstGame = responseWrapper.response.filter{(it.teams.home == winnersFromRoundOf16[0] && 
                            it.teams.away == winnersFromRoundOf16[1])}[0]
                gamesToCreate.add(
                    Game(winnersFromRoundOf16[0],
                        winnersFromRoundOf16[1],
                        "Quarter-Finals",
                        true,
                        firstGame.fixture.timestamp.toLong(),
                        firstGame.fixture.id,
                        1))
                
                val secondGame = responseWrapper.response.filter{(it.teams.home == winnersFromRoundOf16[2] && 
                            it.teams.away == winnersFromRoundOf16[3])}[0]
                gamesToCreate.add(
                    Game(winnersFromRoundOf16[2],
                        winnersFromRoundOf16[3],
                        "Quarter-Finals",
                        true,
                        secondGame.fixture.timestamp.toLong(),
                        secondGame.fixture.id,
                        2))
                
                val thirdGame = responseWrapper.response.filter{(it.teams.home == winnersFromRoundOf16[4] && 
                            it.teams.away == winnersFromRoundOf16[5])}[0]
                gamesToCreate.add(
                    Game(winnersFromRoundOf16[2],
                        winnersFromRoundOf16[3],
                        "Quarter-Finals",
                        true,
                        thirdGame.fixture.timestamp.toLong(),
                        thirdGame.fixture.id,
                        3))
                
                val fourthGame = responseWrapper.response.filter{(it.teams.home == winnersFromRoundOf16[6] && 
                            it.teams.away == winnersFromRoundOf16[7])}[0]
                gamesToCreate.add(
                    Game(winnersFromRoundOf16[6],
                        winnersFromRoundOf16[7],
                        "Quarter-Finals",
                        true,
                        fourthGame.fixture.timestamp.toLong(),
                        fourthGame.fixture.id,
                        4))
                gameRepository.saveAll(gamesToCreate)
            }
            "Semi-Finals" -> {
                val request = BuildNewRequest("${Constants.FIXTURES_API}&round = Semi-Finals","GET",null,"x-rapidapi-host",Constants.X_RAPID_API_HOST,"x-rapidapi-key",Constants.FOOTBALL_API_KEY)
                val gamesToCreate = mutableListOf<Game>()
                val response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
                val responseWrapper : FixturesAPIResponseWrapper = Gson().fromJson(response.body(), FixturesAPIResponseWrapper::class.java)
                val winnersFromQuarterFinals = mutableListOf<Team>()
                val quarterFinals = gameRepository.findByGroupOrderByGroupAsc("Quarter-Finals");
                for (game in quarterFinals) {
                    winnersFromQuarterFinals.add(game.winner!!)
                }

                val firstGame = responseWrapper.response.filter{(it.teams.home == winnersFromQuarterFinals[0] && 
                            it.teams.away == winnersFromQuarterFinals[1])}[0]
                gamesToCreate.add(
                    Game(winnersFromQuarterFinals[0],
                        winnersFromQuarterFinals[1],
                        "Semi-Finals",
                        true,
                        firstGame.fixture.timestamp.toLong(),
                        firstGame.fixture.id,
                        1))
                
                val secondGame = responseWrapper.response.filter{(it.teams.home == winnersFromQuarterFinals[2] && 
                            it.teams.away == winnersFromQuarterFinals[3])}[0]
                gamesToCreate.add(
                    Game(winnersFromQuarterFinals[2],
                        winnersFromQuarterFinals[3],
                        "Semi-Finals",
                        true,
                        secondGame.fixture.timestamp.toLong(),
                        secondGame.fixture.id,
                        2))
                
                gameRepository.saveAll(gamesToCreate)
            }
            "Final" -> {
                val request = BuildNewRequest("${Constants.FIXTURES_API}&round = Final","GET",null,"x-rapidapi-host",Constants.X_RAPID_API_HOST,"x-rapidapi-key",Constants.FOOTBALL_API_KEY)
                val response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
                val responseWrapper : FixturesAPIResponseWrapper = Gson().fromJson(response.body(), FixturesAPIResponseWrapper::class.java)
                val winnersFromSemiFinals = mutableListOf<Team>()
                val semiFinals = gameRepository.findByGroupOrderByGroupAsc("Semi-Finals");
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
}