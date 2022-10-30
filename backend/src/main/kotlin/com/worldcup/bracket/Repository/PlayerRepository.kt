package com.worldcup.bracket.Repository

import org.springframework.data.mongodb.repository.MongoRepository
import com.worldcup.bracket.Entity.Player
import org.springframework.stereotype.Repository;
import org.springframework.data.mongodb.repository.Query;

interface PlayerRepository : MongoRepository<Player,String>{
    @Query(value = "{'team.id' : ?0}")
    fun findAllPlayersOnTeam(teamId : String) : List<Player> 
}