package com.worldcup.bracket.repository

import org.springframework.data.mongodb.repository.MongoRepository
import com.worldcup.bracket.Entity.Game
import org.springframework.stereotype.Repository
import org.springframework.data.mongodb.repository.Query;

import java.util.Date

interface GameRepository : MongoRepository<Game,String>{
    fun findByOrderByDateAsc() : List<Game>

    @Query("{\$and :[{\$or:[{'home._id': ?0 }, { 'away._id' : ?0}]},{'date': {\$gte: ?1}}]}")
    fun getAllGamesFromTeamUpcoming(teamId: String, date: Long) : List<Game>

    @Query("{\$and :[{\$or:[{'home._id': ?0 }, { 'away._id' : ?0}]},{'date': {\$lt: ?1}}]}")
    fun getAllGamesFromTeamPast(teamId: String, date: Long) : List<Game>

    @Query("{\$or :[{\$and:[{'home.name': ?0 }, { 'away.name' : ?1}]},{\$and:[{'home.name': ?1 }, { 'away.name' : ?0}]}]}")
    fun getAllGamesBetweenTwoTeams(team1Name: String, team2Name: String) : List<Game>

    fun findByGroupOrderByGroupAsc(group: String) : List<Game>
    // used for knockout stage games
}