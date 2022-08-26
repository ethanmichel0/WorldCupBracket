package com.worldcup.bracket.Entity

import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.annotation.Id
import org.bson.types.ObjectId

@Document(collection="teams")
class Team (name: String){
    @Id
    var id: String? = ObjectId().toHexString()
    var name: String = name
    override fun equals(other: Any?): Boolean =
        other is Team && other.name == name
}