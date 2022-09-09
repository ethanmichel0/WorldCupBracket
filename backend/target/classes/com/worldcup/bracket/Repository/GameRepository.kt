package com.worldcup.bracket.repository

import org.springframework.data.mongodb.repository.MongoRepository
import com.worldcup.bracket.Entity.Game
import org.springframework.stereotype.Repository
import org.springframework.data.mongodb.repository.Query;

import java.util.Date

interface GameRepository : MongoRepository<Game,String>{
    fun findByOrderByDateAsc() : List<Game>

    @Query("{\$and :[{\$or:[{'team1._id': ?0 }, { 'team2._id' : ?0}]},{'date': {\$gte: ?1}}]}")
    fun getAllGamesFromTeamUpcoming(teamName: String, date: Long) : List<Game>
    @Query("{\$and :[{\$or:[{'team1._id': ?0 }, { 'team2._id' : ?0}]},{'date': {\$lt: ?1}}]}")
    fun getAllGamesFromTeamPast(teamName: String, date: Long) : List<Game>
}