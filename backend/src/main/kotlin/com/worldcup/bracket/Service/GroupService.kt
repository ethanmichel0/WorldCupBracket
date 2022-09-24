package com.worldcup.bracket.Service

import com.worldcup.bracket.repository.GameRepository
import com.worldcup.bracket.repository.TeamRepository
import com.worldcup.bracket.repository.GroupRepository

import com.worldcup.bracket.Entity.Group
import com.worldcup.bracket.Entity.Team

import com.worldcup.bracket.DTO.OverrideGroupSettings

class GroupService(private val gameRepository : GameRepository, 
    private val teamRepository : TeamRepository,
    private val groupRepository : GroupRepository) {
    fun sortTeamsInGroup(group : Group) {
        val pointsComparison = compareByDescending<Team>{it.pointsGroup}
            .thenByDescending{it.goalsForGroup - it.goalsAgainstGroup}
            .thenByDescending{it.goalsForGroup}
            .thenComparator({a,b -> 
                orderTwoTeamsByRelatedGame(a,b)
            })
        group.teams.sortedWith(pointsComparison)
    }

    fun orderTwoTeamsByRelatedGame(team1 : Team, team2: Team) : Int {
        val relatedGame = gameRepository.getAllGamesBetweenTwoTeams(team1.name,team2.name)[0]
        return if (relatedGame.home == team1) relatedGame.awayScore - relatedGame.homeScore else relatedGame.homeScore - relatedGame.awayScore
    }

    // In case teams are still tied after trying to figure out which teams should advance
    fun overrideGroupOrdering(overrideGroupSettings : OverrideGroupSettings) : Group {
        if (overrideGroupSettings.overridePass != System.getenv("OVERRIDE_PW")) throw IllegalArgumentException("You need special permission to set tiebreakers")
        if (! (teamRepository.existsByName(overrideGroupSettings.first) && teamRepository.existsByName(overrideGroupSettings.second) && 
            teamRepository.existsByName(overrideGroupSettings.third) && teamRepository.existsByName(overrideGroupSettings.fourth)
            && groupRepository.existsByLetter(overrideGroupSettings.groupLetter))) {
            throw IllegalArgumentException("Teams Provided Do Not Match")
        }

        val team1 : Team = teamRepository.findByName(overrideGroupSettings.first)[0]
        val team2 : Team = teamRepository.findByName(overrideGroupSettings.second)[0]
        val team3 : Team = teamRepository.findByName(overrideGroupSettings.third)[0]
        val team4 : Team = teamRepository.findByName(overrideGroupSettings.fourth)[0]

        val relevantGroup = groupRepository.findByLetter(overrideGroupSettings.groupLetter)[0]
        if (! (relevantGroup.teams.contains(team1)
            && relevantGroup.teams.contains(team2)
            && relevantGroup.teams.contains(team3)
            && relevantGroup.teams.contains(team4))) {
                throw IllegalArgumentException("Teams Provided Do Not Match")
        }

        relevantGroup.teams[0] = team1
        relevantGroup.teams[1] = team1
        relevantGroup.teams[2] = team3
        relevantGroup.teams[3] = team4

        return relevantGroup
    }
}