package com.worldcup.bracket.Entity

import org.springframework.data.annotation.Id
import org.bson.types.ObjectId

import com.fasterxml.jackson.databind.ser.std.ToStringSerializer
import com.fasterxml.jackson.databind.annotation.JsonSerialize

import org.springframework.data.mongodb.core.mapping.Document

@Document(collection="playerseasons")
data class PlayerSeason(
    val player: Player,
    val teamSeason: TeamSeason,
    val position: Position,
    val number: Int,
    var playerLeftClubDuringSeason: Boolean = false,
    @Id
    @JsonSerialize(using = ToStringSerializer::class) 
    val id: ObjectId = ObjectId.get()
) {
    var gamesPlayed: Int = 0
    var gamesStarted: Int = 0
    var goals : Int = 0
    var assists : Int = 0
    var totalPoints: Int = 0

    override fun equals(other: Any?): Boolean =
        other is PlayerSeason && other.player.id == player.id && other.teamSeason.id == teamSeason.id
}

enum class Position {
    Goalkeeper,
    Defender,
    Midfielder,
    Attacker
}