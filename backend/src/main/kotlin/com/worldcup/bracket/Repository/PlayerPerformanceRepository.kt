package com.worldcup.bracket.Repository

import org.springframework.data.mongodb.repository.MongoRepository
import com.worldcup.bracket.Entity.PlayerPerformance
import org.springframework.stereotype.Repository;

interface PlayerPerformanceRepository : MongoRepository<PlayerPerformance,String>{

}