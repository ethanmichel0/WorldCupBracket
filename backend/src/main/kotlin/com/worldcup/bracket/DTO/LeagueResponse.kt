package com.worldcup.bracket.DTO

data class LeagueResponse(
    val response : List<League>
)

data class League(
    val league: LeagueNested,
    val country: CountryNested,
    val seasons: List<SeasonsNested>
)

data class LeagueNested(
    val name: String,
    val id: String,
    val logo: String,
)

data class CountryNested(
    val name: String
)

data class SeasonsNested(
    val year: Int
)
