package com.worldcup.bracket.Entity
import org.springframework.data.mongodb.core.mapping.Document
import org.bson.types.ObjectId
import org.springframework.data.annotation.Id

@Document(collection="playertrades")
data class PlayerTrade (
    val offeringPlayer: String,
    val receivingPlayer: String,
    val playersOffering: List<String>,
    val playersRequesting: List<String>,
) {
    var state = TradeState.Offered
}

enum class TradeState {
    Offered,
    Accepted,
    Declined
}