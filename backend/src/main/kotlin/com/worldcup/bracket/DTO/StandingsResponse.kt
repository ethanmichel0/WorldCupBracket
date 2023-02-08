package com.worldcup.bracket.DTO


data class StandingsResponse(
    val response: List<StandingsNested>
)

data class StandingsNested(
    val league: LeagueNestedWithinStandings
)

data class LeagueNestedWithinStandings(
    val standings: List<List<Standings>> 
)

// for world cup and other tournaments with groups double nesting allows rankings by groups

data class Standings(
    val team: TeamInfo,
    val group: String,
    val rank: Int
)

data class TeamInfo(
    val id: String,
    val name: String,
    val logo: String
)

/* Here is the relevant JSON 
{
    "get": "standings",
    "parameters": {
        "league": "39",
        "season": "2022"
    },
    "errors": [],
    "results": 1,
    "paging": {
        "current": 1,
        "total": 1
    },
    "response": [
        {
            "league": {
                "id": 39,
                "name": "Premier League",
                "country": "England",
                "logo": "https://media-3.api-sports.io/football/leagues/39.png",
                "flag": "https://media-3.api-sports.io/flags/gb.svg",
                "season": 2022,
                "standings": [
                    [
                        {
                            "rank": 1,
                            "team": {
                                "id": 42,
                                "name": "Arsenal",
                                "logo": "https://media-3.api-sports.io/football/teams/42.png"
                            },
                            "points": 50,
                            "goalsDiff": 29,
                            "group": "Premier League",
                            "form": "WWDWW",
                            "status": "same",
                            "description": "Promotion - Champions League (Group Stage)",
                            "all": {
                                "played": 19,
                                "win": 16,
                                "draw": 2,
                                "lose": 1,
                                "goals": {
                                    "for": 45,
                                    "against": 16
                                }
                            },
                            "home": {
                                "played": 9,
                                "win": 8,
                                "draw": 1,
                                "lose": 0,
                                "goals": {
                                    "for": 25,
                                    "against": 10
                                }
                            },
                            "away": {
                                "played": 10,
                                "win": 8,
                                "draw": 1,
                                "lose": 1,
                                "goals": {
                                    "for": 20,
                                    "against": 6
                                }
                            },
                            "update": "2023-01-23T00:00:00+00:00"
                        }, ...
                        // all other teams added here
                    ]
                ]
            }
        }
    ]
} */