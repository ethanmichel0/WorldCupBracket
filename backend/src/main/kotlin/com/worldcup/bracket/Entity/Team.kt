package com.worldcup.bracket.Entity

import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.annotation.Id
import org.bson.types.ObjectId

@Document(collection="teams")
class Team (var name: String){
    @Id
    var id: String? = null  // will be same as Id used in football api for simplicity and hence passed in
    var logo: String? = null // used for https://v3.football.api-sports.io
    var wins: Int = 0
    var losses: Int = 0
    var ties: Int = 0
    val totalGames: Int
        get() = wins + losses + ties
    var goalsFor: Int = 0
    var goalsAgainst: Int = 0
    override fun equals(other: Any?): Boolean =
        other is Team && other.name == name
}