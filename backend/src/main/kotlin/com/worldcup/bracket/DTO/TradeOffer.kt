package com.worldcup.bracket.DTO 

data class TradeOffer (
    val playerDraftReceivingOffer : String?,
    val requestedPlayers : List<String>,
    val offeredPlayers : List<String>) 

// playerDraftReceivingOffer will be null if user is trying to trade for players that are currently undrafted 
