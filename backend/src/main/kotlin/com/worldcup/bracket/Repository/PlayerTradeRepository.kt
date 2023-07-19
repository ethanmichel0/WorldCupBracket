package com.worldcup.bracket.Repository

import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.data.mongodb.repository.Query
import com.worldcup.bracket.Entity.PlayerTrade
import org.springframework.stereotype.Repository


interface PlayerTradeRepository : MongoRepository<PlayerTrade,String>{
    @Query("{\$and :[{\$or:[{'offeringPlayer': ?0 }, { 'receivingPlayer' : ?0}]},{'state': 'Offered'}]}")
    fun findAllActiveTradesInvolvingUser(involvedPlayer: String) : List<PlayerTrade>
}