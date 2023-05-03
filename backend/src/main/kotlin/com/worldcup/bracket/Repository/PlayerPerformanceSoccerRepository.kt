package com.worldcup.bracket.Repository

import org.springframework.data.mongodb.repository.MongoRepository
import com.worldcup.bracket.Entity.PlayerPerformanceSoccer
import org.springframework.stereotype.Repository;
import org.springframework.data.mongodb.repository.Query;

interface PlayerPerformanceSoccerRepository : MongoRepository<PlayerPerformanceSoccer,String>{
    @Query(value = "{'playerSeason.player.id' : ?0, 'game.fixtureId' : ?1}")
    fun findPlayerPerformanceByPlayerAndGame(playerId: String, gameId: String) : List<PlayerPerformanceSoccer> 

    @Query(value = "{'game.fixtureId' : ?0}")
    fun findAllPlayerPerformancesByGame(fixtureId: String) : List<PlayerPerformanceSoccer>

    @Query(value = "{'playerSeason.player.id' : ?0, 'playerSeason.teamSeason.current': true}")
    fun findAllPlayerPerformancesByPlayerDuringCurrentSeason(playerId: String) : List<PlayerPerformanceSoccer>
}