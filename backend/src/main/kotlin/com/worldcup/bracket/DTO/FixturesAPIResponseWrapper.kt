package com.worldcup.bracket.DTO
import com.worldcup.bracket.Entity.Team

data class FixturesAPIResponseWrapper(
    val response : List<FixturesWrapper>
)

data class FixturesWrapper(
    val fixture: FixtureDTO,
    val teams: TeamsNested,
    val goals: GoalsNested,
    val score: ScoreNested,
    val players: List<AllPlayers>?,
    val events: List<AllEvents>? 
) 
// events and players only included in response when a single fixture id is given. 
// For multiple fixtures they are omitted

data class FixtureDTO(
    val timestamp: Long,
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
    val short: String,
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
    val passes: PassesInfo,
    val tackles: TacklesInfo,
    val duels: DuelsInfo,
    val dribbles: DribblesInfo,
    val fouls: FoulsInfo,
    val cards: CardsInfo,
    val penalty: PenaltyInfo
)

data class GamesForPlayerInfo(
    val minutes: Int?,
    val rating: Double?,
    val substitute: Boolean,
    val number: Int?
)

data class GoalsInfo(
    val total: Int?,
    val conceded: Int?,
    val assists: Int?,
    val saves: Int?
)

data class PassesInfo(
    val total: Int?,
    val key: Int?,
    val accuracy: Int?
)

data class TacklesInfo(
    val total: Int?,
    val blocks: Int?,
    val interceptions: Int?
)

data class DuelsInfo(
    val total: Int?,
    val won: Int?
)

data class DribblesInfo(
    val attempts: Int?,
    val success: Int?,
    val past: Int?
)

data class FoulsInfo(
    val drawn: Int?,
    val committed: Int?
)

data class CardsInfo(
    val yellow: Int?,
    val red: Int?
)

data class PenaltyInfo(
    val won: Int?,
    val commited: Int?,
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

/* 
{
    "get": "fixtures",
    "parameters": {
        "id": "868036"
    },
    "errors": [],
    "results": 1,
    "paging": {
        "current": 1,
        "total": 1
    },
    "response": [
        {
            "fixture": {
                "id": 868036,
                "referee": "Salisbury Michael, England",
                "timezone": "UTC",
                "date": "2022-10-08T14:00:00+00:00",
                "timestamp": 1665237600,
                "periods": {
                    "first": 1665237600,
                    "second": 1665241200
                },
                "venue": {
                    "id": 504,
                    "name": "Vitality Stadium",
                    "city": "Bournemouth, Dorset"
                },
                "status": {
                    "long": "Match Finished",
                    "short": "FT",
                    "elapsed": 90
                }
            },
            "league": {
                "id": 39,
                "name": "Premier League",
                "country": "England",
                "logo": "https://media.api-sports.io/football/leagues/39.png",
                "flag": "https://media.api-sports.io/flags/gb.svg",
                "season": 2022,
                "round": "Regular Season - 10"
            },
            "teams": {
                "home": {
                    "id": 35,
                    "name": "Bournemouth",
                    "logo": "https://media.api-sports.io/football/teams/35.png",
                    "winner": true
                },
                "away": {
                    "id": 46,
                    "name": "Leicester",
                    "logo": "https://media.api-sports.io/football/teams/46.png",
                    "winner": false
                }
            },
            "goals": {
                "home": 2,
                "away": 1
            },
            "score": {
                "halftime": {
                    "home": 0,
                    "away": 1
                },
                "fulltime": {
                    "home": 2,
                    "away": 1
                },
                "extratime": {
                    "home": null,
                    "away": null
                },
                "penalty": {
                    "home": null,
                    "away": null
                }
            },
            "events": [
                {
                    "time": {
                        "elapsed": 10,
                        "extra": null
                    },
                    "team": {
                        "id": 46,
                        "name": "Leicester",
                        "logo": "https://media.api-sports.io/football/teams/46.png"
                    },
                    "player": {
                        "id": 1098,
                        "name": "P. Daka"
                    },
                    "assist": {
                        "id": null,
                        "name": null
                    },
                    "type": "Goal",
                    "detail": "Normal Goal",
                    "comments": null
                },
                {
                    "time": {
                        "elapsed": 11,
                        "extra": null
                    },
                    "team": {
                        "id": 46,
                        "name": "Leicester",
                        "logo": "https://media.api-sports.io/football/teams/46.png"
                    },
                    "player": {
                        "id": 1098,
                        "name": "Patson Daka"
                    },
                    "assist": {
                        "id": null,
                        "name": null
                    },
                    "type": "Var",
                    "detail": "Goal confirmed",
                    "comments": null
                },
                {
                    "time": {
                        "elapsed": 40,
                        "extra": null
                    },
                    "team": {
                        "id": 35,
                        "name": "Bournemouth",
                        "logo": "https://media.api-sports.io/football/teams/35.png"
                    },
                    "player": {
                        "id": 18815,
                        "name": "Ryan Fredericks"
                    },
                    "assist": {
                        "id": null,
                        "name": null
                    },
                    "type": "Card",
                    "detail": "Yellow Card",
                    "comments": "Simulation"
                },
                {
                    "time": {
                        "elapsed": 59,
                        "extra": null
                    },
                    "team": {
                        "id": 46,
                        "name": "Leicester",
                        "logo": "https://media.api-sports.io/football/teams/46.png"
                    },
                    "player": {
                        "id": 22233,
                        "name": "Boubakary Soumaré"
                    },
                    "assist": {
                        "id": null,
                        "name": null
                    },
                    "type": "Card",
                    "detail": "Yellow Card",
                    "comments": "Foul"
                },
                {
                    "time": {
                        "elapsed": 67,
                        "extra": null
                    },
                    "team": {
                        "id": 46,
                        "name": "Leicester",
                        "logo": "https://media.api-sports.io/football/teams/46.png"
                    },
                    "player": {
                        "id": 1098,
                        "name": "P. Daka"
                    },
                    "assist": {
                        "id": 18788,
                        "name": "J. Vardy"
                    },
                    "type": "subst",
                    "detail": "Substitution 1",
                    "comments": null
                },
                {
                    "time": {
                        "elapsed": 67,
                        "extra": null
                    },
                    "team": {
                        "id": 46,
                        "name": "Leicester",
                        "logo": "https://media.api-sports.io/football/teams/46.png"
                    },
                    "player": {
                        "id": 2920,
                        "name": "T. Castagne"
                    },
                    "assist": {
                        "id": 3421,
                        "name": "D. Amartey"
                    },
                    "type": "subst",
                    "detail": "Substitution 2",
                    "comments": null
                },
                {
                    "time": {
                        "elapsed": 68,
                        "extra": null
                    },
                    "team": {
                        "id": 35,
                        "name": "Bournemouth",
                        "logo": "https://media.api-sports.io/football/teams/35.png"
                    },
                    "player": {
                        "id": 2734,
                        "name": "P. Billing"
                    },
                    "assist": {
                        "id": null,
                        "name": null
                    },
                    "type": "Goal",
                    "detail": "Normal Goal",
                    "comments": null
                },
                {
                    "time": {
                        "elapsed": 71,
                        "extra": null
                    },
                    "team": {
                        "id": 35,
                        "name": "Bournemouth",
                        "logo": "https://media.api-sports.io/football/teams/35.png"
                    },
                    "player": {
                        "id": 1125,
                        "name": "R. Christie"
                    },
                    "assist": {
                        "id": 18883,
                        "name": "D. Solanke"
                    },
                    "type": "Goal",
                    "detail": "Normal Goal",
                    "comments": null
                },
                {
                    "time": {
                        "elapsed": 72,
                        "extra": null
                    },
                    "team": {
                        "id": 35,
                        "name": "Bournemouth",
                        "logo": "https://media.api-sports.io/football/teams/35.png"
                    },
                    "player": {
                        "id": 18815,
                        "name": "R. Fredericks"
                    },
                    "assist": {
                        "id": 19769,
                        "name": "J. Stacey"
                    },
                    "type": "subst",
                    "detail": "Substitution 1",
                    "comments": null
                },
                {
                    "time": {
                        "elapsed": 77,
                        "extra": null
                    },
                    "team": {
                        "id": 35,
                        "name": "Bournemouth",
                        "logo": "https://media.api-sports.io/football/teams/35.png"
                    },
                    "player": {
                        "id": 2734,
                        "name": "P. Billing"
                    },
                    "assist": {
                        "id": 196855,
                        "name": "J. Anthony"
                    },
                    "type": "subst",
                    "detail": "Substitution 2",
                    "comments": null
                },
                {
                    "time": {
                        "elapsed": 79,
                        "extra": null
                    },
                    "team": {
                        "id": 46,
                        "name": "Leicester",
                        "logo": "https://media.api-sports.io/football/teams/46.png"
                    },
                    "player": {
                        "id": 22233,
                        "name": "B. Soumaré"
                    },
                    "assist": {
                        "id": 2778,
                        "name": "K. Ịheanachọ"
                    },
                    "type": "subst",
                    "detail": "Substitution 3",
                    "comments": null
                },
                {
                    "time": {
                        "elapsed": 86,
                        "extra": null
                    },
                    "team": {
                        "id": 46,
                        "name": "Leicester",
                        "logo": "https://media.api-sports.io/football/teams/46.png"
                    },
                    "player": {
                        "id": 19760,
                        "name": "J. Justin"
                    },
                    "assist": {
                        "id": 18906,
                        "name": "Ayoze Pérez"
                    },
                    "type": "subst",
                    "detail": "Substitution 4",
                    "comments": null
                },
                {
                    "time": {
                        "elapsed": 88,
                        "extra": null
                    },
                    "team": {
                        "id": 46,
                        "name": "Leicester",
                        "logo": "https://media.api-sports.io/football/teams/46.png"
                    },
                    "player": {
                        "id": 18784,
                        "name": "James Maddison"
                    },
                    "assist": {
                        "id": null,
                        "name": null
                    },
                    "type": "Card",
                    "detail": "Yellow Card",
                    "comments": "Foul"
                },
                {
                    "time": {
                        "elapsed": 90,
                        "extra": 2
                    },
                    "team": {
                        "id": 35,
                        "name": "Bournemouth",
                        "logo": "https://media.api-sports.io/football/teams/35.png"
                    },
                    "player": {
                        "id": 18883,
                        "name": "D. Solanke"
                    },
                    "assist": {
                        "id": 19804,
                        "name": "K. Moore"
                    },
                    "type": "subst",
                    "detail": "Substitution 3",
                    "comments": null
                }
            ],
            "lineups": [
                {
                    "team": {
                        "id": 35,
                        "name": "Bournemouth",
                        "logo": "https://media.api-sports.io/football/teams/35.png",
                        "colors": {
                            "player": {
                                "primary": "c4211f",
                                "number": "ffffff",
                                "border": "c4211f"
                            },
                            "goalkeeper": {
                                "primary": "008000",
                                "number": "ffffff",
                                "border": "008000"
                            }
                        }
                    },
                    "coach": {
                        "id": 18151,
                        "name": "G. O'Neil",
                        "photo": "https://media.api-sports.io/football/coachs/18151.png"
                    },
                    "formation": "4-4-2",
                    "startXI": [
                        {
                            "player": {
                                "id": 912,
                                "name": "Neto",
                                "number": 13,
                                "pos": "G",
                                "grid": "1:1"
                            }
                        },
                        {
                            "player": {
                                "id": 18815,
                                "name": "R. Fredericks",
                                "number": 2,
                                "pos": "D",
                                "grid": "2:4"
                            }
                        },
                        {
                            "player": {
                                "id": 18866,
                                "name": "C. Mepham",
                                "number": 6,
                                "pos": "D",
                                "grid": "2:3"
                            }
                        },
                        {
                            "player": {
                                "id": 6610,
                                "name": "M. Senesi",
                                "number": 25,
                                "pos": "D",
                                "grid": "2:2"
                            }
                        },
                        {
                            "player": {
                                "id": 18869,
                                "name": "A. Smith",
                                "number": 15,
                                "pos": "D",
                                "grid": "2:1"
                            }
                        },
                        {
                            "player": {
                                "id": 1125,
                                "name": "R. Christie",
                                "number": 10,
                                "pos": "M",
                                "grid": "3:4"
                            }
                        },
                        {
                            "player": {
                                "id": 18872,
                                "name": "L. Cook",
                                "number": 4,
                                "pos": "M",
                                "grid": "3:3"
                            }
                        },
                        {
                            "player": {
                                "id": 2490,
                                "name": "J. Lerma",
                                "number": 8,
                                "pos": "M",
                                "grid": "3:2"
                            }
                        },
                        {
                            "player": {
                                "id": 19245,
                                "name": "M. Tavernier",
                                "number": 16,
                                "pos": "M",
                                "grid": "3:1"
                            }
                        },
                        {
                            "player": {
                                "id": 2734,
                                "name": "P. Billing",
                                "number": 29,
                                "pos": "F",
                                "grid": "4:2"
                            }
                        },
                        {
                            "player": {
                                "id": 18883,
                                "name": "D. Solanke",
                                "number": 9,
                                "pos": "F",
                                "grid": "4:1"
                            }
                        }
                    ],
                    "substitutes": [
                        {
                            "player": {
                                "id": 19769,
                                "name": "J. Stacey",
                                "number": 17,
                                "pos": "D",
                                "grid": null
                            }
                        },
                        {
                            "player": {
                                "id": 196855,
                                "name": "J. Anthony",
                                "number": 32,
                                "pos": "F",
                                "grid": null
                            }
                        },
                        {
                            "player": {
                                "id": 19804,
                                "name": "K. Moore",
                                "number": 21,
                                "pos": "F",
                                "grid": null
                            }
                        },
                        {
                            "player": {
                                "id": 18940,
                                "name": "J. Stephens",
                                "number": 3,
                                "pos": "D",
                                "grid": null
                            }
                        },
                        {
                            "player": {
                                "id": 19824,
                                "name": "J. Zemura",
                                "number": 33,
                                "pos": "D",
                                "grid": null
                            }
                        },
                        {
                            "player": {
                                "id": 18860,
                                "name": "M. Travers",
                                "number": 1,
                                "pos": "G",
                                "grid": null
                            }
                        },
                        {
                            "player": {
                                "id": 19866,
                                "name": "J. Lowe",
                                "number": 18,
                                "pos": "F",
                                "grid": null
                            }
                        },
                        {
                            "player": {
                                "id": 19964,
                                "name": "S. Dembélé",
                                "number": 20,
                                "pos": "M",
                                "grid": null
                            }
                        },
                        {
                            "player": {
                                "id": 19356,
                                "name": "E. Marcondes",
                                "number": 11,
                                "pos": "M",
                                "grid": null
                            }
                        }
                    ]
                },
                {
                    "team": {
                        "id": 46,
                        "name": "Leicester",
                        "logo": "https://media.api-sports.io/football/teams/46.png",
                        "colors": {
                            "player": {
                                "primary": "84d1d7",
                                "number": "ffffff",
                                "border": "84d1d7"
                            },
                            "goalkeeper": {
                                "primary": "4a4a4a",
                                "number": "ffffff",
                                "border": "4a4a4a"
                            }
                        }
                    },
                    "coach": {
                        "id": 15,
                        "name": "B. Rodgers",
                        "photo": "https://media.api-sports.io/football/coachs/15.png"
                    },
                    "formation": "4-1-4-1",
                    "startXI": [
                        {
                            "player": {
                                "id": 18146,
                                "name": "D. Ward",
                                "number": 1,
                                "pos": "G",
                                "grid": "1:1"
                            }
                        },
                        {
                            "player": {
                                "id": 2920,
                                "name": "T. Castagne",
                                "number": 27,
                                "pos": "D",
                                "grid": "2:4"
                            }
                        },
                        {
                            "player": {
                                "id": 18772,
                                "name": "J. Evans",
                                "number": 6,
                                "pos": "D",
                                "grid": "2:3"
                            }
                        },
                        {
                            "player": {
                                "id": 8694,
                                "name": "W. Faes",
                                "number": 3,
                                "pos": "D",
                                "grid": "2:2"
                            }
                        },
                        {
                            "player": {
                                "id": 19760,
                                "name": "J. Justin",
                                "number": 2,
                                "pos": "D",
                                "grid": "2:1"
                            }
                        },
                        {
                            "player": {
                                "id": 22233,
                                "name": "B. Soumaré",
                                "number": 42,
                                "pos": "M",
                                "grid": "3:1"
                            }
                        },
                        {
                            "player": {
                                "id": 18784,
                                "name": "J. Maddison",
                                "number": 10,
                                "pos": "M",
                                "grid": "4:4"
                            }
                        },
                        {
                            "player": {
                                "id": 2926,
                                "name": "Y. Tielemans",
                                "number": 8,
                                "pos": "M",
                                "grid": "4:3"
                            }
                        },
                        {
                            "player": {
                                "id": 148099,
                                "name": "K. Dewsbury-Hall",
                                "number": 22,
                                "pos": "M",
                                "grid": "4:2"
                            }
                        },
                        {
                            "player": {
                                "id": 18778,
                                "name": "H. Barnes",
                                "number": 7,
                                "pos": "M",
                                "grid": "4:1"
                            }
                        },
                        {
                            "player": {
                                "id": 1098,
                                "name": "P. Daka",
                                "number": 20,
                                "pos": "F",
                                "grid": "5:1"
                            }
                        }
                    ],
                    "substitutes": [
                        {
                            "player": {
                                "id": 18788,
                                "name": "J. Vardy",
                                "number": 9,
                                "pos": "F",
                                "grid": null
                            }
                        },
                        {
                            "player": {
                                "id": 3421,
                                "name": "D. Amartey",
                                "number": 18,
                                "pos": "D",
                                "grid": null
                            }
                        },
                        {
                            "player": {
                                "id": 2778,
                                "name": "K. Ịheanachọ",
                                "number": 14,
                                "pos": "F",
                                "grid": null
                            }
                        },
                        {
                            "player": {
                                "id": 18906,
                                "name": "Ayoze Pérez",
                                "number": 17,
                                "pos": "F",
                                "grid": null
                            }
                        },
                        {
                            "player": {
                                "id": 18777,
                                "name": "M. Albrighton",
                                "number": 11,
                                "pos": "M",
                                "grid": null
                            }
                        },
                        {
                            "player": {
                                "id": 2925,
                                "name": "D. Praet",
                                "number": 26,
                                "pos": "M",
                                "grid": null
                            }
                        },
                        {
                            "player": {
                                "id": 289624,
                                "name": "S. Braybrooke",
                                "number": 44,
                                "pos": "M",
                                "grid": null
                            }
                        },
                        {
                            "player": {
                                "id": 152969,
                                "name": "L. Thomas",
                                "number": 33,
                                "pos": "D",
                                "grid": null
                            }
                        },
                        {
                            "player": {
                                "id": 17736,
                                "name": "D. Iversen",
                                "number": 31,
                                "pos": "G",
                                "grid": null
                            }
                        }
                    ]
                }
            ],
            "statistics": [
                {
                    "team": {
                        "id": 35,
                        "name": "Bournemouth",
                        "logo": "https://media.api-sports.io/football/teams/35.png"
                    },
                    "statistics": [
                        {
                            "type": "Shots on Goal",
                            "value": 4
                        },
                        {
                            "type": "Shots off Goal",
                            "value": 2
                        },
                        {
                            "type": "Total Shots",
                            "value": 10
                        },
                        {
                            "type": "Blocked Shots",
                            "value": 4
                        },
                        {
                            "type": "Shots insidebox",
                            "value": 8
                        },
                        {
                            "type": "Shots outsidebox",
                            "value": 2
                        },
                        {
                            "type": "Fouls",
                            "value": 8
                        },
                        {
                            "type": "Corner Kicks",
                            "value": 7
                        },
                        {
                            "type": "Offsides",
                            "value": 3
                        },
                        {
                            "type": "Ball Possession",
                            "value": "44%"
                        },
                        {
                            "type": "Yellow Cards",
                            "value": 1
                        },
                        {
                            "type": "Red Cards",
                            "value": null
                        },
                        {
                            "type": "Goalkeeper Saves",
                            "value": 4
                        },
                        {
                            "type": "Total passes",
                            "value": 424
                        },
                        {
                            "type": "Passes accurate",
                            "value": 359
                        },
                        {
                            "type": "Passes %",
                            "value": "85%"
                        }
                    ]
                },
                {
                    "team": {
                        "id": 46,
                        "name": "Leicester",
                        "logo": "https://media.api-sports.io/football/teams/46.png"
                    },
                    "statistics": [
                        {
                            "type": "Shots on Goal",
                            "value": 5
                        },
                        {
                            "type": "Shots off Goal",
                            "value": 2
                        },
                        {
                            "type": "Total Shots",
                            "value": 9
                        },
                        {
                            "type": "Blocked Shots",
                            "value": 2
                        },
                        {
                            "type": "Shots insidebox",
                            "value": 8
                        },
                        {
                            "type": "Shots outsidebox",
                            "value": 1
                        },
                        {
                            "type": "Fouls",
                            "value": 16
                        },
                        {
                            "type": "Corner Kicks",
                            "value": 2
                        },
                        {
                            "type": "Offsides",
                            "value": null
                        },
                        {
                            "type": "Ball Possession",
                            "value": "56%"
                        },
                        {
                            "type": "Yellow Cards",
                            "value": 2
                        },
                        {
                            "type": "Red Cards",
                            "value": null
                        },
                        {
                            "type": "Goalkeeper Saves",
                            "value": 2
                        },
                        {
                            "type": "Total passes",
                            "value": 551
                        },
                        {
                            "type": "Passes accurate",
                            "value": 475
                        },
                        {
                            "type": "Passes %",
                            "value": "86%"
                        }
                    ]
                }
            ],
            "players": [
                {
                    "team": {
                        "id": 35,
                        "name": "Bournemouth",
                        "logo": "https://media.api-sports.io/football/teams/35.png",
                        "update": "2023-03-03T04:07:58+00:00"
                    },
                    "players": [
                        {
                            "player": {
                                "id": 912,
                                "name": "Neto",
                                "photo": "https://media.api-sports.io/football/players/912.png"
                            },
                            "statistics": [
                                {
                                    "games": {
                                        "minutes": 90,
                                        "number": 13,
                                        "position": "G",
                                        "rating": "6.9",
                                        "captain": false,
                                        "substitute": false
                                    },
                                    "offsides": null,
                                    "shots": {
                                        "total": null,
                                        "on": null
                                    },
                                    "goals": {
                                        "total": null,
                                        "conceded": 1,
                                        "assists": null,
                                        "saves": 4
                                    },
                                    "passes": {
                                        "total": 23,
                                        "key": null,
                                        "accuracy": "12"
                                    },
                                    "tackles": {
                                        "total": null,
                                        "blocks": null,
                                        "interceptions": null
                                    },
                                    "duels": {
                                        "total": null,
                                        "won": null
                                    },
                                    "dribbles": {
                                        "attempts": null,
                                        "success": null,
                                        "past": null
                                    },
                                    "fouls": {
                                        "drawn": null,
                                        "committed": null
                                    },
                                    "cards": {
                                        "yellow": 0,
                                        "red": 0
                                    },
                                    "penalty": {
                                        "won": null,
                                        "commited": null,
                                        "scored": 0,
                                        "missed": 0,
                                        "saved": 0
                                    }
                                }
                            ]
                        },
                        {
                            "player": {
                                "id": 18815,
                                "name": "Ryan Fredericks",
                                "photo": "https://media.api-sports.io/football/players/18815.png"
                            },
                            "statistics": [
                                {
                                    "games": {
                                        "minutes": 72,
                                        "number": 2,
                                        "position": "D",
                                        "rating": "6.2",
                                        "captain": false,
                                        "substitute": false
                                    },
                                    "offsides": null,
                                    "shots": {
                                        "total": null,
                                        "on": null
                                    },
                                    "goals": {
                                        "total": null,
                                        "conceded": 0,
                                        "assists": null,
                                        "saves": null
                                    },
                                    "passes": {
                                        "total": 33,
                                        "key": null,
                                        "accuracy": "25"
                                    },
                                    "tackles": {
                                        "total": null,
                                        "blocks": null,
                                        "interceptions": null
                                    },
                                    "duels": {
                                        "total": 3,
                                        "won": null
                                    },
                                    "dribbles": {
                                        "attempts": 1,
                                        "success": null,
                                        "past": null
                                    },
                                    "fouls": {
                                        "drawn": null,
                                        "committed": 1
                                    },
                                    "cards": {
                                        "yellow": 1,
                                        "red": 0
                                    },
                                    "penalty": {
                                        "won": null,
                                        "commited": null,
                                        "scored": 0,
                                        "missed": 0,
                                        "saved": null
                                    }
                                }
                            ]
                        },
                        {
                            "player": {
                                "id": 18866,
                                "name": "Chris Mepham",
                                "photo": "https://media.api-sports.io/football/players/18866.png"
                            },
                            "statistics": [
                                {
                                    "games": {
                                        "minutes": 90,
                                        "number": 6,
                                        "position": "D",
                                        "rating": "7",
                                        "captain": false,
                                        "substitute": false
                                    },
                                    "offsides": null,
                                    "shots": {
                                        "total": 1,
                                        "on": null
                                    },
                                    "goals": {
                                        "total": null,
                                        "conceded": 0,
                                        "assists": null,
                                        "saves": null
                                    },
                                    "passes": {
                                        "total": 60,
                                        "key": null,
                                        "accuracy": "54"
                                    },
                                    "tackles": {
                                        "total": 2,
                                        "blocks": null,
                                        "interceptions": 1
                                    },
                                    "duels": {
                                        "total": 4,
                                        "won": 3
                                    },
                                    "dribbles": {
                                        "attempts": null,
                                        "success": null,
                                        "past": null
                                    },
                                    "fouls": {
                                        "drawn": null,
                                        "committed": null
                                    },
                                    "cards": {
                                        "yellow": 0,
                                        "red": 0
                                    },
                                    "penalty": {
                                        "won": null,
                                        "commited": null,
                                        "scored": 0,
                                        "missed": 0,
                                        "saved": null
                                    }
                                }
                            ]
                        },
                        {
                            "player": {
                                "id": 6610,
                                "name": "Marcos Senesi",
                                "photo": "https://media.api-sports.io/football/players/6610.png"
                            },
                            "statistics": [
                                {
                                    "games": {
                                        "minutes": 90,
                                        "number": 25,
                                        "position": "D",
                                        "rating": "7.3",
                                        "captain": false,
                                        "substitute": false
                                    },
                                    "offsides": null,
                                    "shots": {
                                        "total": null,
                                        "on": null
                                    },
                                    "goals": {
                                        "total": null,
                                        "conceded": 0,
                                        "assists": null,
                                        "saves": null
                                    },
                                    "passes": {
                                        "total": 53,
                                        "key": null,
                                        "accuracy": "49"
                                    },
                                    "tackles": {
                                        "total": 3,
                                        "blocks": 2,
                                        "interceptions": 1
                                    },
                                    "duels": {
                                        "total": 4,
                                        "won": 3
                                    },
                                    "dribbles": {
                                        "attempts": null,
                                        "success": null,
                                        "past": null
                                    },
                                    "fouls": {
                                        "drawn": null,
                                        "committed": null
                                    },
                                    "cards": {
                                        "yellow": 0,
                                        "red": 0
                                    },
                                    "penalty": {
                                        "won": null,
                                        "commited": null,
                                        "scored": 0,
                                        "missed": 0,
                                        "saved": null
                                    }
                                }
                            ]
                        },
                        {
                            "player": {
                                "id": 18869,
                                "name": "Adam Smith",
                                "photo": "https://media.api-sports.io/football/players/18869.png"
                            },
                            "statistics": [
                                {
                                    "games": {
                                        "minutes": 90,
                                        "number": 15,
                                        "position": "D",
                                        "rating": "6.7",
                                        "captain": true,
                                        "substitute": false
                                    },
                                    "offsides": null,
                                    "shots": {
                                        "total": null,
                                        "on": null
                                    },
                                    "goals": {
                                        "total": null,
                                        "conceded": 0,
                                        "assists": null,
                                        "saves": null
                                    },
                                    "passes": {
                                        "total": 34,
                                        "key": null,
                                        "accuracy": "29"
                                    },
                                    "tackles": {
                                        "total": 1,
                                        "blocks": null,
                                        "interceptions": 4
                                    },
                                    "duels": {
                                        "total": 4,
                                        "won": 2
                                    },
                                    "dribbles": {
                                        "attempts": 1,
                                        "success": null,
                                        "past": 1
                                    },
                                    "fouls": {
                                        "drawn": 1,
                                        "committed": null
                                    },
                                    "cards": {
                                        "yellow": 0,
                                        "red": 0
                                    },
                                    "penalty": {
                                        "won": null,
                                        "commited": null,
                                        "scored": 0,
                                        "missed": 0,
                                        "saved": null
                                    }
                                }
                            ]
                        },
                        {
                            "player": {
                                "id": 1125,
                                "name": "Ryan Christie",
                                "photo": "https://media.api-sports.io/football/players/1125.png"
                            },
                            "statistics": [
                                {
                                    "games": {
                                        "minutes": 90,
                                        "number": 10,
                                        "position": "M",
                                        "rating": "7",
                                        "captain": false,
                                        "substitute": false
                                    },
                                    "offsides": null,
                                    "shots": {
                                        "total": 3,
                                        "on": 3
                                    },
                                    "goals": {
                                        "total": 1,
                                        "conceded": 0,
                                        "assists": null,
                                        "saves": null
                                    },
                                    "passes": {
                                        "total": 37,
                                        "key": null,
                                        "accuracy": "32"
                                    },
                                    "tackles": {
                                        "total": 1,
                                        "blocks": null,
                                        "interceptions": 1
                                    },
                                    "duels": {
                                        "total": 16,
                                        "won": 5
                                    },
                                    "dribbles": {
                                        "attempts": 4,
                                        "success": 1,
                                        "past": 3
                                    },
                                    "fouls": {
                                        "drawn": 2,
                                        "committed": 2
                                    },
                                    "cards": {
                                        "yellow": 0,
                                        "red": 0
                                    },
                                    "penalty": {
                                        "won": null,
                                        "commited": null,
                                        "scored": 0,
                                        "missed": 0,
                                        "saved": null
                                    }
                                }
                            ]
                        },
                    ]
                }
            ]
        }
    ]
}
*/