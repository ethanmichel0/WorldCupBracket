package com.worldcup.bracket.Entity
import org.springframework.data.mongodb.core.mapping.Document
import org.bson.types.ObjectId
import org.springframework.data.annotation.Id

import com.fasterxml.jackson.databind.ser.std.ToStringSerializer
import com.fasterxml.jackson.databind.annotation.JsonSerialize

@Document(collection="playertrades")
data class PlayerTrade (
    val offeringPlayer: String,
    val receivingPlayer: String?, // if the player you are trade for is currently unowned by anyone in the group this will be null
    val playersOffering: List<String>,
    val playersRequesting: List<String>,
    var state : TradeState = TradeState.Offered,
    @Id
    @JsonSerialize(using = ToStringSerializer::class) 
    val id: ObjectId = ObjectId.get()
) {
}

enum class TradeState {
    Offered,
    Accepted,
    Declined
}