package com.worldcup.bracket.Repository

import org.springframework.data.mongodb.repository.MongoRepository
import com.worldcup.bracket.Entity.TeamSeason
import org.springframework.stereotype.Repository;

import org.springframework.data.mongodb.repository.Query;

interface TeamSeasonRepository : MongoRepository<TeamSeason,String>{

    @Query(value = "{'league._id' : ?0}")
    fun findAllTeamSeasonsByLeague(league: String) : List<TeamSeason>

    @Query(value = "{'season' : ?0, 'league._id' : ?1}")
    fun findAllTeamSeasonsBySeasonAndLeague(season: Int, league: String) : List<TeamSeason>

    @Query(value = "{'season' : ?0, 'league._id' : ?1, group: ?2}")
    fun findAllTeamSeasonsBySeasonAndLeagueAndGroup(season: Int, league: String, group: String) : List<TeamSeason>

    @Query(value = "{'season' : ?0, 'team.id' : ?1}")
    fun findTeamSeasonBySeasonAndTeam(season: Int, team: String) : List<TeamSeason>

    @Query(value = "{'current' : true, 'league.id' : ?0}")
    fun findAllCurrentTeamSeasonsByLeague(league: String) : List<TeamSeason>

    @Query(value= "{'current' : true, 'team.id' : ?0}")
    fun findAllCurrentTeamSeasonsByTeam(team: String) : List<TeamSeason>
}