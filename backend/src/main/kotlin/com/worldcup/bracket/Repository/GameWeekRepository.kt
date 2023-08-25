package com.worldcup.bracket.Repository

import org.springframework.data.mongodb.repository.MongoRepository
import com.worldcup.bracket.Entity.GameWeek
import org.springframework.stereotype.Repository


interface GameWeekRepository : MongoRepository<GameWeek,String>{
    fun findTopByEndGreaterThanEqualOrderByEnd(end: Long) : GameWeek
    fun findTopByGameWeekName(gameWeekName: String) : GameWeek
}