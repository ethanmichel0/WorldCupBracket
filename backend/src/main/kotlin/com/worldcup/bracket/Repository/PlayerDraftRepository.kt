package com.worldcup.bracket.Repository

import org.springframework.data.mongodb.repository.MongoRepository
import com.worldcup.bracket.Entity.PlayerDraft
import org.springframework.stereotype.Repository;

interface PlayerDraftRepository : MongoRepository<PlayerDraft,String>{
    
}