package com.worldcup.bracket.Repository

import org.springframework.data.mongodb.repository.MongoRepository
import com.worldcup.bracket.Entity.League
import org.springframework.stereotype.Repository;
import org.springframework.data.mongodb.repository.Query;

interface LeagueRepository : MongoRepository<League,String> {
}