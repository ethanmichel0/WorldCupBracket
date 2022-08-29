package com.worldcup.bracket.Entity

import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.annotation.Id
import org.bson.types.ObjectId

@Document(collection="teams")
class Team (var name: String){
    @Id
    var id: String? = null  // will be same as Id used in football api for simplicity and hence passed in
    var logo: String? = null // used for https://v3.football.api-sports.io
    override fun equals(other: Any?): Boolean =
        other is Team && other.name == name
}