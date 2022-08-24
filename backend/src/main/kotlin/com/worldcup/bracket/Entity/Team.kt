package com.worldcup.bracket.Entity

import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.annotation.Id
import org.bson.types.ObjectId

@Document(collection="teams")
class Team (name: String){
    @Id
    val id: String? = ObjectId().toHexString()
    val name: String = name
}