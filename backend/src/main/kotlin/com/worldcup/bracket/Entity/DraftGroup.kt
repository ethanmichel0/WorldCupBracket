package com.worldcup.bracket.Entity

import org.springframework.data.annotation.Id
import org.springframework.data.annotation.Transient
import org.springframework.data.mongodb.core.mapping.DocumentReference
import org.springframework.data.mongodb.core.mapping.Document
import com.fasterxml.jackson.annotation.JsonIgnore
import java.time.Instant
import com.worldcup.bracket.DTO.UserInfo

import org.bson.types.ObjectId


@Document(collection="draftgroups")
data class DraftGroup(
    @Id 
    val id: ObjectId = ObjectId.get(),
    val name: String,
    var owner: User,
    @JsonIgnore
    val password: String,
    var draftTime: Long = -1,
    val members: MutableList<String> = mutableListOf<String>(),
    // this is storing emails, which are unique to a User entity.
    var availablePlayers: MutableList<PlayerSeason> = mutableListOf<PlayerSeason>(),
    val leagues: MutableList<League>,
    val season: Int,
    var amountOfTimeEachTurn: Int = 60,
    var numberOfPlayersEachTeam: Int = 15
) {
    var indexOfCurrentUser: Int = 0
    var numberPlayersDrafted: Int = 0
    val draftComplete: Boolean
        get() = this.members.size * this.numberOfPlayersEachTeam == this.numberPlayersDrafted
    val draftOngoing: Boolean
        get() = ! this.draftComplete && Instant.now().getEpochSecond() >= this.draftTime
}
    