package com.worldcup.bracket.DTO 

data class TradeOffer (
    val offeredPlayers : List<String>,
    val requestedPlayers : List<String>,
    val playerDraftReceivingOffer : String,
    val groupName : String
)