package com.worldcup.bracket.Entity

import org.springframework.data.annotation.Id
import org.springframework.data.annotation.Transient
import org.springframework.data.mongodb.core.mapping.Document
import com.fasterxml.jackson.annotation.JsonIgnore

import org.bson.types.ObjectId


@Document(collection="draftgroups")
data class DraftGroup(
    @Id 
    val id : ObjectId = ObjectId.get(),
    val name: String, 
    var owner: User, 
    val createdDate: Long = System.currentTimeMillis() / 1000,
    @JsonIgnore
    val password: String,
    @JsonIgnore
    var draftTime: Long = -1,
    var members: MutableList<User> = mutableListOf<User>(),
    var availablePlayers: MutableList<PlayerSeason> = mutableListOf<PlayerSeason>(),
    val leagues: MutableList<League>,
    val season: Int
) {
    var amountOfTimeEachTurn: Int = 60
    var numberOfPlayersEachTeam: Int = 15
    var indexOfCurrentUser: Int = -1
    var numberPlayersDrafted: Int = 0
    val draftComplete: Boolean
        get() = members.size * numberOfPlayersEachTeam == numberPlayersDrafted
}
    