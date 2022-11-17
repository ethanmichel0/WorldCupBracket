package com.worldcup.bracket.DTO
import com.worldcup.bracket.Entity.Team

data class FixturesAPIResponseWrapper(
    val response : List<FixturesWrapper>
)

data class FixturesWrapper(
    val fixture : FixtureDTO,
    val teams: TeamsNested,
    val goals: GoalsNested,
    val score: ScoreNested,
    val players: List<AllPlayers>?,
    val events: List<AllEvents>? 
) 
// events and players only included in response when a single fixture id is given. 
// For multiple fixtures they are omitted

data class FixtureDTO(
    val timestamp: String,
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

data class AllPlayers( // an array for players on each team
    val team: Team,
    val players: List<PlayersNested>
)

data class PlayersNested( // all players per team
    val player: PlayerInfo,
    val statistics: List<PlayerStatistics>
)

data class PlayerInfo(
    val id: String,
    val name: String
)

data class PlayerStatistics(
    val games: GamesForPlayerInfo,
    val goals: GoalsInfo,
    val cards: CardsInfo,
    val penalty: PenaltyInfo
)

data class GamesForPlayerInfo(
    val minutes: Int?,
    val rating: Double?,
    val substitute: Boolean
)

data class GoalsInfo(
    val total: Int?,
    val conceded: Int?,
    val assists: Int?,
    val saves: Int?
)

data class CardsInfo(
    val yellow: Int?,
    val red: Int?
)

data class PenaltyInfo(
    val won: Int?,
    val committed: Int?,
    val scored: Int?,
    val missed: Int?,
    val saved: Int?
)

data class AllEvents ( // only will use to track own goals as all other events accounted for
    val team: Team,
    val player: PlayerInfo,
    val detail: String
)
// only will be useful when "detail" is own goal representing an own goal.