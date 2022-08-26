package com.worldcup.bracket.repository

import org.springframework.data.mongodb.repository.MongoRepository
import com.worldcup.bracket.Entity.Group
import org.springframework.stereotype.Repository;

interface GroupRepository : MongoRepository<Group,String>{

}