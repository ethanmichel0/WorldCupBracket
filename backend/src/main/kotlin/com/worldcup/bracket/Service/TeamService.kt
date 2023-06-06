package com.worldcup.bracket.Service

import com.worldcup.bracket.Entity.Team
import com.worldcup.bracket.Repository.TeamRepository
import com.worldcup.bracket.Repository.GameRepository
import com.worldcup.bracket.DTO.OverrideGroupSettings
import com.worldcup.bracket.SecretsConfigurationProperties

import org.springframework.stereotype.Service


@Service
class TeamService(private val teamRepository : TeamRepository, 
    private val gameRepository: GameRepository,
    private val secretsConfigurationProperties: SecretsConfigurationProperties) {

    /*
    public fun getCurrentSquadOfTeam() {
        val playersRequest = BuildNewRequest(footballAPIData.getAllPlayersOnTeamEndpoint(teamSeason.team.id),"GET",null,"x-rapidapi-host",footballAPIData.X_RAPID_API_HOST,"x-rapidapi-key",footballAPIData.FOOTBALL_API_KEY)
        val playersResponse = httpClient.send(playersRequest, HttpResponse.BodyHandlers.ofString());
        val playersResponseWrapper : PlayersAPIResponseWrapper = Gson().fromJson(playersResponse.body(), PlayersAPIResponseWrapper::class.java)
        
        val playersToAddToDB = mutableListOf<Player>()
        val playersSeasonsToAddToDB = mutableListOf<PlayerSeason>()

        for (player in playersResponseWrapper.response[0].players) {
            // check if player already in database
            val relevantPlayerToAddToDB = Player(
                name = player.name,
                id = player.id
            )

            val relevantPlayerSeason = PlayerSeason(
                player = relevantPlayerToAddToDB,
                teamSeason = teamSeason,
                position = player.position,
                number = player.number
            )

            if (playerRepository.findByIdOrNull(player.id)==null) {
                playersToAddToDB.add(relevantPlayerToAddToDB)
            }

            // add all players for beginning of season and all new transferred players after transfer breaks during the season
            if (firstTimeAddingTeamThisSeason || playerSeasonRepository.findPlayerSeasonBySeasonAndPlayer(teamSeason.season,player.id).size == 0) {
                playersSeasonsToAddToDB.add(relevantPlayerSeason)
            }
        }

        // change "current" property to false on all players that transferred out of club during transfer windows if this is during transfer window
        // this can be done by checking all player ids of current squad from api and comparing against those in data base to confirm players are still playing for club
        
        if (!firstTimeAddingTeamThisSeason) {
            val allCurrentPlayerIds : List<String> = playersResponseWrapper.response[0].players.map{it.id}
            playerSeasonRepository.findAllPlayerSeasonsByTeamSeason(teamSeason.id.toString()).filter{! allCurrentPlayerIds.contains(it.player.id) }.
                forEach{
                    it.current = false
                    playersSeasonsToAddToDB.add(it)
                }
        }
        return Pair(playersToAddToDB,playersSeasonsToAddToDB)
    }
    */
}