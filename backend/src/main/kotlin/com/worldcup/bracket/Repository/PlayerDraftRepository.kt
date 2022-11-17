package com.worldcup.bracket.Repository

import org.springframework.data.mongodb.repository.MongoRepository
import com.worldcup.bracket.Entity.PlayerDraft
import org.springframework.stereotype.Repository;
import org.springframework.data.mongodb.repository.Query;

interface PlayerDraftRepository : MongoRepository<PlayerDraft,String>{
    @Query(value = "{'draftGroup.id' : ?0")
    fun findAllPlayerDraftsByGroup(draftGroupId: String) : List<PlayerDraft> 
}