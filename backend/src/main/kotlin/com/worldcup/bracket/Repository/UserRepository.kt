package com.worldcup.bracket.Repository

import org.bson.types.ObjectId

import org.springframework.data.mongodb.repository.MongoRepository
import com.worldcup.bracket.Entity.User
import org.springframework.stereotype.Repository;

interface UserRepository : MongoRepository<User,String>{
    fun findByName(name: String) : List<User>
    fun findByEmail(email: String) : List<User>
    fun findByEmailIn(ids: List<String>) : List<User>
    fun findByPrincipalId(principal: String) : List<User>
}