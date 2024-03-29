package com.worldcup.bracket.Repository

import org.bson.types.ObjectId

import org.springframework.data.mongodb.repository.MongoRepository
import com.worldcup.bracket.Entity.DraftGroup
import org.springframework.stereotype.Repository

import java.util.Date

interface DraftGroupRepository : MongoRepository<DraftGroup,String>{
    fun findByName(name: String) : List<DraftGroup>
    fun findByIdIn(ids: List<ObjectId>) : List<DraftGroup>
}