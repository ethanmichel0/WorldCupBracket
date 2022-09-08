package com.worldcup.bracket.Service 

import org.springframework.stereotype.Service
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import java.util.Date
import com.worldcup.bracket.Entity.Game

@Service
class GameService(private val mongoTemplate: MongoTemplate) {

    private fun updateScores(fixtureId : String) {
        
    }

    public fun getAllGamesFromTeamUpcoming(teamName: String?) : List<Game>{
        val criteria = Criteria()
        var team1CriteriaNameMatch : Criteria = Criteria.where("team1").not().ne(teamName)
        var team2CriteriaNameMatch : Criteria = Criteria.where("team2").not().ne(teamName)

        // NOTE that there appears to be a bug here. for some reason calling "is()" as an instance method of criteria
        // is not workingwith the error message "expected element". Pretty sure this is a bug so double negation is needed
        // TODO fill out bug report

        team1CriteriaNameMatch = team1CriteriaNameMatch.isNull()
        criteria.andOperator(
            criteria.orOperator(team1CriteriaNameMatch,team2CriteriaNameMatch),
            Criteria.where("date").gte(Date())
        )
        val query = Query(criteria)
        
        return mongoTemplate.find(query,Game::class.java,"games")
    }

    public fun getAllGamesFromTeamPast(teamName: String?) : List<Game>{
        val criteria = Criteria()
        var team1CriteriaNameMatch : Criteria = Criteria.where("team1").not().ne(teamName)
        var team2CriteriaNameMatch : Criteria = Criteria.where("team2").not().ne(teamName)

        // NOTE that there appears to be a bug here. for some reason calling "is()" as an instance method of criteria
        // is not workingwith the error message "expected element". Pretty sure this is a bug so double negation is needed

        team1CriteriaNameMatch = team1CriteriaNameMatch.isNull()
        criteria.andOperator(
            criteria.orOperator(team1CriteriaNameMatch,team2CriteriaNameMatch),
            Criteria.where("date").lt(Date())
        )
        val query = Query(criteria)
        
        return mongoTemplate.find(query,Game::class.java,"games")
    }
}