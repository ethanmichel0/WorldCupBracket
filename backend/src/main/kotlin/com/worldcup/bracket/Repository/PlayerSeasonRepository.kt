package com.worldcup.bracket.Repository

import org.springframework.data.mongodb.repository.MongoRepository
import com.worldcup.bracket.Entity.PlayerSeason
import org.springframework.stereotype.Repository;

interface PlayerSeasonRepository : MongoRepository<PlayerSeason,String>{
    @Query(value = "{'teamSeason.season' : ?0, 'player.id' : ?1}")
    fun findAllPlayerSeasonsBySeasonAndPlayer(season: Int, player: String) : List<PlayerSeason>

    @Query(value = "{'teamSeason.id' : ?1}")
    fun findAllPlayerSeasonsByTeamSeason(teamSeason: String) : List<PlayerSeason>
}