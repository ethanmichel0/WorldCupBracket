package com.worldcup.bracket.Repository

import org.springframework.data.mongodb.repository.MongoRepository
import com.worldcup.bracket.Entity.PlayerPerformance
import org.springframework.stereotype.Repository;
import org.springframework.data.mongodb.repository.Query;

interface PlayerPerformanceRepository : MongoRepository<PlayerPerformance,String>{
    @Query(value = "{'player.id' : ?0, 'game.fixtureId' : ?1}")
    fun findAllPlayerPerformancesByPlayerAndGame(playerId: Int, gameId: String) : List<PlayerPerformance> 

    @Query(value = "{'game.fixtureId' : ?0}")
    fun findAllPlayerPerformancesByGame(fixtureId: String) : List<PlayerPerformance>
}