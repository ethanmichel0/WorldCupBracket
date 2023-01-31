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
        private val secretsConfigurationProperties: SecretsConfigurationProperties){
    fun sortTeamsInGroup(groupLetter : String) {
        val group : MutableList<Team> = teamRepository.findByGroup(groupLetter).toMutableList()
        val pointsComparison = compareByDescending<Team>{it.pointsGroup}
            .thenByDescending{it.goalsDifference}
            .thenByDescending{it.goalsFor}
        group.sortWith(pointsComparison)
        group[0].positionGroup = 1
        group[1].positionGroup = 2
        group[2].positionGroup = 3
        group[3].positionGroup = 4

        // there are more tie breakers but honestly easier to specify which teams
        // should go through manually using below function

        teamRepository.saveAll(listOf(group[0],group[1],group[2],group[3]))
    }

    // In case teams are still tied after trying to figure out which teams should advance
    fun overrideGroupOrdering(overrideGroupSettings : OverrideGroupSettings) : List<Team> {
        if (overrideGroupSettings.overridePass != secretsConfigurationProperties.overridePw) throw IllegalArgumentException("You need special permission to set tiebreakers")

        if (! (teamRepository.existsByName(overrideGroupSettings.first) && teamRepository.existsByName(overrideGroupSettings.second) 
                && teamRepository.existsByName(overrideGroupSettings.third) && teamRepository.existsByName(overrideGroupSettings.fourth))) {
            throw IllegalArgumentException("Teams Provided Do Not Match")
        }

        val team1 : Team = teamRepository.findByName(overrideGroupSettings.first)[0]
        val team2 : Team = teamRepository.findByName(overrideGroupSettings.second)[0]
        val team3 : Team = teamRepository.findByName(overrideGroupSettings.third)[0]
        val team4 : Team = teamRepository.findByName(overrideGroupSettings.fourth)[0]

        val relevantGroup : List<Team> = teamRepository.findByGroup(overrideGroupSettings.groupLetter)
        if (! (relevantGroup.contains(team1)
            && relevantGroup.contains(team2)
            && relevantGroup.contains(team3)
            && relevantGroup.contains(team4))) {
                throw IllegalArgumentException("Teams Provided Do Not Match")
        }

        team1.positionGroup = 1
        team2.positionGroup = 2
        team3.positionGroup = 3
        team4.positionGroup = 4

        teamRepository.saveAll(listOf(team1,team2, team3, team4))

        return listOf(team1,team2,team3,team4)
    }
}