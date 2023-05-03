package com.worldcup.bracket.Repository

import org.springframework.data.mongodb.repository.MongoRepository
import com.worldcup.bracket.Entity.PlayerDraft
import org.springframework.stereotype.Repository;
import org.springframework.data.mongodb.repository.Query;

interface PlayerDraftRepository : MongoRepository<PlayerDraft,String>{
    @Query(value = "{'draftGroup.name' : ?0}")
    fun findAllPlayerDraftsByGroup(draftGroupName: String) : List<PlayerDraft> 
    @Query(value = "{'draftGroup.name' : ?0, 'user.id' : ?1}")
    fun findPlayerDraftByGroupAndUser(draftGroupName: String, userId: String) : List<PlayerDraft> 
}