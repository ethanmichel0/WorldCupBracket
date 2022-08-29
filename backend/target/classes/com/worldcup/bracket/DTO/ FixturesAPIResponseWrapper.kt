package com.worldcup.bracket.DTO

import com.google.gson.annotations.SerializedName
import com.worldcup.bracket.Entity.Team

data class FixturesAPIResponseWrapper(
    val response : List<FixturesWrapper>
)

data class FixturesWrapper(
    val fixture : FixtureDTO,
    val teams: TeamsNested
)

data class FixtureDTO(
    val timestamp : String,
    val id: String
)

data class TeamsNested(
    val home: Team,
    val away: Team
)