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
    val userName: String,
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
    // these two fields will be indexed, whereas pointEarnersByGameWeek and nonPointEarnersByGameWeek will not be indexed
    // since those fields don't need to be searched
    var pointsByCurrentStarters: Map<String,Int> = mapOf<String,Int>(),
    var pointsByCurrentSubs: Map<String,Pair<Int,Int>> = mapOf<String,Pair<Int,Int>>(),
    // contains Pair(subPriority of player (1st elgible to sub in, etc), points in game week). This will allow us to determine which subs should score points for team in case starters don't play
    
    var gameWeekSummary: MutableMap<String,GameWeekSummary> = mutableMapOf<String,GameWeekSummary>()
) {
    val draftedPlayersAllPositions: List<PlayerSeason>
        get() = draftedGoalkeepers + draftedDefenders + draftedMidfielders + draftedForwards
}

enum class Formation{
    FOURFOURTWO,
    FOURTHREETHREE,
    FOURTWOFOUR,
    FOURFIVEONE,
    THREEFOURTHREE,
    THREEFIVETWO,
    FIVETHREETWO,
    FIVEFOURONE
}

data class GameWeekSummary(
    var formation: String,
    val pointEarners: Map<String,Int> = mutableMapOf<String,Int>(),
    var nonPointEarners: MutableMap<String,Pair<Int,Int>> = mutableMapOf<String,Pair<Int,Int>>()
    // contains Pair(subPriority of player (1st elgible to sub in, etc), points in game week). This will allow us to determine which subs should score points for team in case starters don't play
)