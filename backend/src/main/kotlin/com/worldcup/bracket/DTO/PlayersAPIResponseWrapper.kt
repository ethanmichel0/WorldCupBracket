package com.worldcup.bracket.DTO

import com.worldcup.bracket.Entity.Position

data class PlayersAPIResponseWrapper(
    val response: List<OuterWrapper>
)

data class OuterWrapper(
    val players: List<PlayersWrapper>
)

data class PlayersWrapper(
    val name: String,
    val age: Int,
    val id: String,
    val number: Int,
    val photo: String,
    val position: Position
)

/*
{
    "get": "players/squads",
    "parameters": {
        "team": "35"
    },
    "errors": [],
    "results": 1,
    "paging": {
        "current": 1,
        "total": 1
    },
    "response": [
        {
            "team": {
                "id": 35,
                "name": "Bournemouth",
                "logo": "https://media.api-sports.io/football/teams/35.png"
            },
            "players": [
                {
                    "id": 18860,
                    "name": "M. Travers",
                    "age": 24,
                    "number": 1,
                    "position": "Goalkeeper",
                    "photo": "https://media.api-sports.io/football/players/18860.png"
                },
            ...

 */