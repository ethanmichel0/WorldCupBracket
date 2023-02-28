package com.worldcup.bracket.Repository

import org.springframework.data.mongodb.repository.MongoRepository
import com.worldcup.bracket.Entity.PlayerSeason
import org.springframework.stereotype.Repository;

import org.springframework.data.mongodb.repository.Query;

interface PlayerSeasonRepository : MongoRepository<PlayerSeason,String>{
    @Query(value = "{'teamSeason.season' : ?0, 'player.id' : ?1}")
    fun findAllPlayerSeasonsBySeasonAndPlayer(season: Int, player: String) : List<PlayerSeason>

    @Query(value = "{'teamSeason.id' : ?0}")
    fun findAllPlayerSeasonsByTeamSeason(teamSeason: String) : List<PlayerSeason>

    @Query(value = "{\$or:[{'teamSeason.id' : ?0},{'teamSeason.id' : ?1}]}")
    fun findAllPlayersFromOneGame(homeTeamSeasonId: String, awayTeamSeasonId: String) : List<PlayerSeason> 

    @Query(value = "{'teamSeason.league.id' : ?0, 'teamSeason.season' : ?1 }")
    fun findAllPlayerSeasonsByLeagueAndSeason(leagueId: String, season: Int) : List<PlayerSeason>

    @Query(value = "{'teamSeason.league.id' : {\$in : ?0}, 'teamSeason.season' : ?1 }")
    fun findAllPlayerSeasonsByLeaguesAndSeason(leagueIds: List<String>, season: Int) : List<PlayerSeason>
}