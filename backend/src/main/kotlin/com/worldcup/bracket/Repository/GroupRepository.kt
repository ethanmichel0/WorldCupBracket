package com.worldcup.bracket.repository

import org.springframework.data.mongodb.repository.MongoRepository
import com.worldcup.bracket.Entity.Group
import org.springframework.stereotype.Repository;

interface GroupRepository : MongoRepository<Group,String>{
    fun findByLetter(letter : String) : List<Group>
    fun existsByLetter(letter : String) : Boolean
}