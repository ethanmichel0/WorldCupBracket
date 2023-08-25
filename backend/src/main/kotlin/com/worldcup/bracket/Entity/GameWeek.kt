package com.worldcup.bracket.Entity

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer
import com.fasterxml.jackson.databind.annotation.JsonSerialize


import org.bson.types.ObjectId


@Document(collection="gameweeks")
data class GameWeek(
    @Id 
    @JsonSerialize(using = ToStringSerializer::class)
    val id: ObjectId = ObjectId.get(),
    var start: Long,
    var end: Long,
    var deadline: Long = start - (24*60*60),
    val gameWeekName: String,
    val league: League
)