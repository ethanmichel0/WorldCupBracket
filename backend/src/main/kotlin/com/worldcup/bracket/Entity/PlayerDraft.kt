package com.worldcup.bracket.Entity

import org.springframework.data.annotation.Id

import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document

import com.fasterxml.jackson.databind.ser.std.ToStringSerializer
import com.fasterxml.jackson.databind.annotation.JsonSerialize

import org.bson.types.ObjectId

import java.util.SortedSet

@Document(collection="playerdrafts")
@CompoundIndex(def = "{'draftGroup.id' : 1, 'userEmail': 1}")

data class PlayerDraft(
    val userEmail: String,
    val draftGroup: DraftGroup,
    @JsonSerialize(using = ToStringSerializer::class)
    @Id 
    val id: ObjectId = ObjectId.get(),
    val players: MutableList<PlayerSeason> = mutableListOf<PlayerSeason>(),
    val draftedForwards: MutableList<PlayerSeason> = mutableListOf<PlayerSeason>(),
    val draftedMidfielders: MutableList<PlayerSeason> = mutableListOf<PlayerSeason>(),
    val draftedDefenders: MutableList<PlayerSeason> = mutableListOf<PlayerSeason>(),
    val draftedGoalkeepers: MutableList<PlayerSeason> = mutableListOf<PlayerSeason>(),
    var watchListUndrafted: MutableList<PlayerSeason> = mutableListOf<PlayerSeason>(),
    var points: Int = 0,
    var performancesByRound: Map<String,Set<ObjectId>> = mutableMapOf<String,Set<ObjectId>>(),
    var pointsByRound: Map<String,Int> = mutableMapOf<String,Int>()
) {
    val draftedPlayersAllPositions: List<PlayerSeason>
        get() = draftedGoalkeepers + draftedDefenders + draftedMidfielders + draftedForwards
}
    