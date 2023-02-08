package com.worldcup.bracket.Repository

import org.springframework.data.mongodb.repository.MongoRepository
import com.worldcup.bracket.Entity.Team
import org.springframework.stereotype.Repository;

interface TeamRepository : MongoRepository<Team,String>{
    fun findByName(name : String) : List<Team>
}