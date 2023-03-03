package com.worldcup.bracket.DTO

import com.worldcup.bracket.Entity.Position

data class PlayerInfoSinglePlayerRequest(
    val response: List<PlayerInfoWrapper>
)

data class PlayerInfoWrapper(
    //val player: PlayerOverview,
    val statistics: List<StatisticsNested>,
) 

data class StatisticsNested(
    val games: StatisticsGames,
    val team: TeamsPlayedForDuringSeason
)

data class StatisticsGames(
    val position: Position
)

data class TeamsPlayedForDuringSeason(
    val id: String,
    val name: String
)

/* 
{
    "get": "players",
    "parameters": {
        "id": "1098",
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
            "player": {
                "id": 1098,
                "name": "P. Daka",
                "firstname": "Patson",
                "lastname": "Daka",
                "age": 25,
                "birth": {
                    "date": "1998-10-09",
                    "place": "Chingola",
                    "country": "Zambia"
                },
                "nationality": "Zambia",
                "height": "183 cm",
                "weight": "71 kg",
                "injured": false,
                "photo": "https://media.api-sports.io/football/players/1098.png"
            },
            "statistics": [
                {
                    "team": {
                        "id": 46,
                        "name": "Leicester",
                        "logo": "https://media.api-sports.io/football/teams/46.png"
                    },
                    "league": {
                        "id": 39,
                        "name": "Premier League",
                        "country": "England",
                        "logo": "https://media.api-sports.io/football/leagues/39.png",
                        "flag": "https://media.api-sports.io/flags/gb.svg",
                        "season": 2022
                    },
                    "games": {
                        "appearences": 18,
                        "lineups": 9,
                        "minutes": 719,
                        "number": null,
                        "position": "Attacker",
                        "rating": "6.844444",
                        "captain": false
                    },
                    "substitutes": {
                        "in": 9,
                        "out": 8,
                        "bench": 14
                    },
                    "shots": {
                        "total": 15,
                        "on": 11
                    },
                    "goals": {
                        "total": 3,
                        "conceded": 0,
                        "assists": 3,
                        "saves": null
                    },
                    "passes": {
                        "total": 156,
                        "key": 10,
                        "accuracy": 5
                    },
                    "tackles": {
                        "total": 8,
                        "blocks": 2,
                        "interceptions": 3
                    },
                    "duels": {
                        "total": 73,
                        "won": 29
                    },
                    "dribbles": {
                        "attempts": 8,
                        "success": 4,
                        "past": null
                    },
                    "fouls": {
                        "drawn": 6,
                        "committed": 12
                    },
                    "cards": {
                        "yellow": 2,
                        "yellowred": 0,
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
        }
    ]
}
*/