package com.worldcup.bracket.Repository

import org.springframework.data.mongodb.repository.MongoRepository
import com.worldcup.bracket.Entity.Game
import org.springframework.stereotype.Repository
import org.springframework.data.mongodb.repository.Query;

import java.util.Date

interface GameRepository : MongoRepository<Game,String>{
    fun findByOrderByDateAsc() : List<Game>

    @Query("{\$and :[{\$or:[{'home.id': ?0 }, { 'away.id' : ?0}]},{'date': {\$gte: ?1}}]}")
    fun getAllGamesFromTeamUpcoming(teamSeason: String, date: Long) : List<Game>

    @Query("{\$and :[{\$or:[{'home.id': ?0 }, { 'away.id' : ?0}]},{'date': {\$lt: ?1}}]}")
    fun getAllGamesFromTeamPast(teamSeason: String, date: Long) : List<Game>

    @Query("{\$and :[{\$or:[{'home.id': ?0 }, { 'away.id' : ?0}]},{'date': {\$lte: ?1, \$gte: ?2}}]}")
    fun getAllGamesForTeamBetweenTwoDates(teamSeason: String, begin: Long, end: Long) : List<Game>

    @Query("{\$or :[{\$and:[{'home.team.name': ?0 }, { 'away.team.name' : ?1}]},{\$and:[{'home.team.name': ?1 }, { 'away.team.name' : ?0}]}]}")
    fun getAllGamesBetweenTwoTeams(team1Name: String, team2Name: String) : List<Game>

    @Query("{'league.id' : ?0, 'home.season': ?1}")
    fun getAllGamesInLeagueForSeason(league: String, season: Int) : List<Game>
}