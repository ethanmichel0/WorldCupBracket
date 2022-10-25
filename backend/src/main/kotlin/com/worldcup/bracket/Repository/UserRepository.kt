package com.worldcup.bracket.Repository

import org.springframework.data.mongodb.repository.MongoRepository
import com.worldcup.bracket.Entity.User
import org.springframework.stereotype.Repository;

interface UserRepository : MongoRepository<User,String>{
    
}