package com.worldcup.bracket.repository

import org.springframework.data.mongodb.repository.MongoRepository
import com.worldcup.bracket.Entity.Game
import org.springframework.stereotype.Repository;


interface GameRepository : MongoRepository<Game,String>{

}