package com.worldcup.bracket.repository

import org.springframework.data.mongodb.repository.MongoRepository
import com.worldcup.bracket.Entity.Team
import org.springframework.stereotype.Repository;

interface TeamRepository : MongoRepository<Team,String>{
    fun findByName(name : String) : List<Team>
    fun existsByName(name: String) : Boolean
    fun findByGroup(group: String) : List<Team>
    fun findByOrderByGroup() : List<Team>
    fun findByPositionGroupOrderByGroupAsc(positionGroup: Int) : List<Team>
    // get first and second place team in each group for knockoout
}