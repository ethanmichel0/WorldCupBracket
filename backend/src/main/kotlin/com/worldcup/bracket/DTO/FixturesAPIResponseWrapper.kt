package com.worldcup.bracket.DTO
import com.worldcup.bracket.Entity.Team

data class FixturesAPIResponseWrapper(
    val response : List<FixturesWrapper>
)

data class FixturesWrapper(
    val fixture : FixtureDTO,
    val teams: TeamsNested,
    val goals: GoalsNested,
    val score: ScoreNested
)

data class FixtureDTO(
    val timestamp : String,
    val id: String,
    val status: StatusNested
)

data class TeamsNested(
    val home: Team,
    val away: Team
)

data class GoalsNested(
    val home: Int?,
    val away: Int?
)

// need a seperate tracker for penalties as if a team wins in penalties then the score is still marked 0-0 without
// a clear winner

data class ScoreNested(
    val penalty: GoalsNested
)

data class StatusNested(
    val long: String,
    val elapsed: Int?
) 
// "Match Finished" indicates that the match is over