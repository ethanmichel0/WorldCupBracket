package com.worldcup.bracket.Repository

import org.bson.types.ObjectId

import org.springframework.data.mongodb.repository.MongoRepository
import com.worldcup.bracket.Entity.PlayerDraft
import org.springframework.stereotype.Repository;
import org.springframework.data.mongodb.repository.Query;

interface PlayerDraftRepository : MongoRepository<PlayerDraft,String>{
    @Query(value = "{'draftGroup.name' : ?0}")
    fun findAllPlayerDraftsByGroup(draftGroupName: String) : List<PlayerDraft> 
    @Query(value = "{'draftGroup.name' : ?0, 'userEmail' : ?1}")
    fun findPlayerDraftByGroupAndUserEmail(draftGroupName: String, userEmail: String) : List<PlayerDraft> 
    @Query(value = "{'watchListUndrafted.player.id' : ?0}")
    fun findAllPlayerDraftsWithPlayerOnWatchList(playerId: String) : List<PlayerDraft>
}